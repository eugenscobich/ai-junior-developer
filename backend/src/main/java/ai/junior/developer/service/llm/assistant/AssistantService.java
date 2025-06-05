package ai.junior.developer.service.llm.assistant;

import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_DESCRIPTION;
import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_FUNCTIONS;
import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_INSTRUCTIONS;
import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_MODEL;
import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_NAME;
import static com.openai.models.beta.threads.messages.Message.Role.Value.ASSISTANT;

import ai.junior.developer.service.llm.LlmService;
import ai.junior.developer.service.model.MessageResponse;
import ai.junior.developer.service.model.MessagesResponse;
import ai.junior.developer.service.model.UserOrAssistantMessageResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.beta.assistants.Assistant;
import com.openai.models.beta.assistants.AssistantCreateParams;
import com.openai.models.beta.assistants.AssistantDeleteParams;
import com.openai.models.beta.threads.Thread;
import com.openai.models.beta.threads.messages.Message;
import com.openai.models.beta.threads.messages.MessageContent;
import com.openai.models.beta.threads.messages.MessageCreateParams;
import com.openai.models.beta.threads.messages.MessageListParams;
import com.openai.models.beta.threads.messages.Text;
import com.openai.models.beta.threads.messages.TextContentBlock;
import com.openai.models.beta.threads.runs.RequiredActionFunctionToolCall;
import com.openai.models.beta.threads.runs.Run;
import com.openai.models.beta.threads.runs.RunCreateParams;
import com.openai.models.beta.threads.runs.RunRetrieveParams;
import com.openai.models.beta.threads.runs.RunStatus;
import com.openai.models.beta.threads.runs.RunSubmitToolOutputsParams;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "service.llm.type", havingValue = "assistant", matchIfMissing = true)
public class AssistantService implements LlmService {

    private final OpenAIClient client;
    private final ToolDispatcher dispatcher;
    private final RunIdTracker runIdTracker;
    private final ThreadTracker threadTracker;

    private Assistant createAssistant(AssistantCreateParams.Builder builder) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, Object>> functions = mapper.readValue(
            ASSISTANT_FUNCTIONS,
            new TypeReference<>() {
            }
        );

        List<FunctionDefinition> toolDefs = new ArrayList<>();

        for (Map<String, Object> func : functions) {
            String name = (String) func.get("name");
            String description = (String) func.get("description");

            Map<String, Object> rawParams = (Map<String, Object>) func.get("parameters");
            Map<String, JsonValue> paramJson = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawParams.entrySet()) {
                paramJson.put(entry.getKey(), JsonValue.from(entry.getValue()));
            }

            FunctionParameters parameters = FunctionParameters.builder()
                .additionalProperties(paramJson)
                .build();

            toolDefs.add(FunctionDefinition.builder()
                .name(name)
                .description(description)
                .parameters(parameters)
                .build());
        }

        for (FunctionDefinition fn : toolDefs) {
            builder.addFunctionTool(fn);
        }

        AssistantCreateParams params = builder.build();
        Assistant assistant = client.beta().assistants().create(params);
        log.info("Assistant is created with Id:" + assistant.id());
        return assistant;
    }

    private void deleteAssistant(String assistantId) {
        client.beta().assistants().delete(AssistantDeleteParams.builder()
            .assistantId(assistantId)
            .build());
    }

    private Map<String, String> executePrompt(String prompt, String assistantId, String threadId) throws Exception {
        client.beta().threads().messages().create(MessageCreateParams.builder()
            .threadId(threadId)
            .role(MessageCreateParams.Role.USER)
            .content(prompt)
            .build());

        Run run = client.beta().threads().runs().create(RunCreateParams.builder()
            .threadId(threadId)
            .assistantId(assistantId)
            .build());
        MDC.put("runId", run.id());
        runIdTracker.track(run.id(), prompt);
        log.info("Run id: {}", run.id());
        int maxRetries = 1000;
        int retryCount = 0;

        while (true) {
            java.lang.Thread.sleep(500);

            retryCount++;
            if (retryCount > maxRetries) {
                throw new IllegalStateException("Run polling exceeded max retries (" + maxRetries + ")");
            }

            run = client.beta().threads().runs().retrieve(RunRetrieveParams.builder()
                .threadId(run.threadId())
                .runId(run.id())
                .build());

            if (RunStatus.REQUIRES_ACTION.equals(run.status())) {
                Run.RequiredAction requiredAction = run.requiredAction()
                    .orElseThrow(() -> new IllegalStateException("Run requires action but none found."));

                Run.RequiredAction.SubmitToolOutputs submitToolOutputs = requiredAction.submitToolOutputs();
                if (submitToolOutputs == null) {
                    throw new IllegalStateException("submitToolOutputs is missing from requiredAction.");
                }

                List<RequiredActionFunctionToolCall> toolCalls = submitToolOutputs.toolCalls();
                List<RunSubmitToolOutputsParams.ToolOutput> toolOutputs = new ArrayList<>();

                for (RequiredActionFunctionToolCall toolCall : toolCalls) {
                    String functionName = toolCall.function().name();
                    String argumentsJson = toolCall.function().arguments();

                    String result = dispatcher.handleToolCall(functionName, argumentsJson, threadId);

                    toolOutputs.add(RunSubmitToolOutputsParams.ToolOutput.builder()
                        .toolCallId(toolCall.id())
                        .output(result.substring(0, Math.min(1000000, result.length())))
                        .build());
                }

                client.beta().threads().runs().submitToolOutputs(RunSubmitToolOutputsParams.builder()
                    .threadId(run.threadId())
                    .runId(run.id())
                    .toolOutputs(toolOutputs)
                    .build());

            }
            if (Set.of(
                RunStatus.COMPLETED,
                RunStatus.FAILED,
                RunStatus.CANCELLED,
                RunStatus.EXPIRED
            ).contains(run.status())) {
                break;
            }
        }

        List<Message> messages = client.beta().threads().messages().list(MessageListParams.builder()
                .threadId(threadId)
                .build())
            .data();
        //log.info(messages.toString());

        String assistantMessage = messages.stream()
            .filter(m -> m.role().value().equals(ASSISTANT))
            .map(Message::content)
            .flatMap(Collection::stream)
            .map(MessageContent::text)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(TextContentBlock::text)
            .map(Text::value)
            .collect(Collectors.joining(" "));
        return Map.of(
            "runId", run.id(),
            "assistantMessage", assistantMessage
        );
    }

    private Thread createThread() {
        Thread thread = client.beta().threads().create();
        log.info("Thread id: {}", thread.id());

        return thread;
    }

    private Assistant findOrCreateAssistant(AssistantCreateParams.Builder assistantParams, String assistantModel) throws IOException {
        String storedHashCreatedAssistant = assistantParams.build().metadata()
            .map(AssistantCreateParams.Metadata::_additionalProperties)
            .map(props -> props.get("instructionHash"))
            .map(JsonValue::asStringOrThrow)
            .orElse(null);
        Assistant assistant = findAssistant(assistantParams.build()._name().asStringOrThrow(), storedHashCreatedAssistant, assistantModel);

        if (assistant == null) {
            return createAssistant(assistantParams);
        }
        return assistant;
    }

    private Assistant findAssistant(String assistantName, String hash, String assistantModel) {
        return client.beta().assistants().list().data().stream()
            .filter(assistant -> assistant.name().isPresent() && assistant.name().get().equals(assistantName))
            .filter(assistant -> hash.equals(
                assistant.metadata()
                    .map(Assistant.Metadata::_additionalProperties)
                    .map(props -> props.get("instructionHash"))
                    .map(JsonValue::asStringOrThrow)
                    .orElse(null)))
            .filter(assistant -> assistantModel == null || assistant.model().equals(assistantModel))
            .findFirst()
            .orElse(null);
    }

    private static AssistantCreateParams.Builder buildAssistantParams(
        String assistantModel, String assistantName,
        String assistantDescription, String assistantInstructions
    ) {
        String hash = hashInstructions(assistantInstructions);

        Map<String, JsonValue> metadataMap = Map.of(
            "instructionHash", JsonValue.from(hash)
        );

        return AssistantCreateParams.builder()
            .model(assistantModel)
            .name(assistantName)
            .description(assistantDescription)
            .instructions(assistantInstructions)
            .metadata(AssistantCreateParams.Metadata.builder()
                .additionalProperties(metadataMap)
                .build());
    }

    private static String hashInstructions(String instructions) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(instructions.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    @Override
    @SneakyThrows
    public String startAThread() {
        var assistant = findOrCreateAssistant(
            AssistantService.buildAssistantParams(
                ASSISTANT_MODEL, ASSISTANT_NAME,
                ASSISTANT_DESCRIPTION, ASSISTANT_INSTRUCTIONS
            ), ASSISTANT_MODEL
        );
        MDC.put("assistantId", assistant.id());
        Thread thread = createThread();
        threadTracker.track(assistant.id(), thread.id());
        MDC.put("threadId", thread.id());
        return thread.id();
    }

    @Override
    public void continueAThread(String threadId) {
        MDC.put("threadId", threadId);
        var assistantId = threadTracker.findAssistantId(threadId);
        MDC.put("assistantId", assistantId);
    }

    @Override
    @SneakyThrows
    public String executePrompt(String prompt, String threadId) {
        var assistantId = threadTracker.findAssistantId(threadId);
        Map<String, String> result = executePrompt(prompt, assistantId, threadId);
        var runId = result.get("runId");
        runIdTracker.track(runId, prompt);
        return result.get("assistantMessage");
    }

    @Override
    public void getLastThreadId() {

    }

    @Override
    public MessagesResponse getThreadMessages(String threadId) {
        List<Message> allMessages = client.beta().threads().messages().list(
            MessageListParams.builder().threadId(threadId).build()).data();

        allMessages.sort(Comparator.comparing(Message::createdAt));

        Map<String, MessageResponse> groupedMessages = new LinkedHashMap<>();
        String currentUserMessageId = null;

        for (Message msg : allMessages) {
            String value = msg.content().stream()
                .map(c -> c.text()
                    .map(TextContentBlock::text)
                    .map(Text::value)
                    .orElse("[empty]"))
                .collect(Collectors.joining("\n"));

            UserOrAssistantMessageResponse messageDto = UserOrAssistantMessageResponse.builder()
                .value(value)
                .createdAt(msg.createdAt())
                .threadId(msg.threadId())
                .runId(msg.runId().isPresent() ? msg.runId().get() : null)
                .build();

            switch (msg.role().value()) {
                case USER -> {
                    currentUserMessageId = msg.id();
                    MessageResponse response = MessageResponse.builder()
                        .userMessage(messageDto)
                        .assistantMessages(new ArrayList<>())
                        .build();
                    groupedMessages.put(currentUserMessageId, response);
                }
                case ASSISTANT -> {
                    if (currentUserMessageId != null) {
                        groupedMessages.get(currentUserMessageId).getAssistantMessages().add(messageDto);
                    } else {
                        log.warn("no user message before: {}", msg);
                    }
                }
                default -> {
                    log.warn("Unhandled role: {}", msg.role());
                }
            }
        }

        return MessagesResponse.builder()
            .messagesList(new ArrayList<>(groupedMessages.values()))
            .build();
    }
}
