package ai.junior.developer.assistant;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.beta.assistants.Assistant;
import com.openai.models.beta.assistants.AssistantCreateParams;
import com.openai.models.beta.assistants.AssistantDeleteParams;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.openai.models.beta.threads.Thread;
import com.openai.models.beta.threads.messages.Message;
import com.openai.models.beta.threads.messages.MessageCreateParams;
import com.openai.models.beta.threads.messages.MessageListParams;
import com.openai.models.beta.threads.runs.RequiredActionFunctionToolCall;
import com.openai.models.beta.threads.runs.Run;
import com.openai.models.beta.threads.runs.RunCreateParams;
import com.openai.models.beta.threads.runs.RunRetrieveParams;
import com.openai.models.beta.threads.runs.RunStatus;
import com.openai.models.beta.threads.runs.RunSubmitToolOutputsParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssistantService {
    private final OpenAIClient client;
    private final ToolDispatcher dispatcher;

    public Assistant createAssistant() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File("assistant_functions.json");

        List<Map<String, Object>> functions = mapper.readValue(
                jsonFile,
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

        AssistantCreateParams.Builder builder = AssistantCreateParams.builder()
                .model("gpt-4o")
                .name("Java Assistant with Tools")
                .instructions("You can call Git automation tools to help the user.");

        for (FunctionDefinition fn : toolDefs) {
            builder.addFunctionTool(fn);
        }

        AssistantCreateParams params = builder.build();
        Assistant assistant = client.beta().assistants().create(params);
        System.out.println("assistantId:" + assistant.id() );
        return assistant;
    }

    public void deleteAssistant(String assistantId) {
        client.beta().assistants().delete(AssistantDeleteParams.builder()
                .assistantId(assistantId)
                .build());
    }

    public String executePrompt(String prompt, String assistantId) throws Exception {
        Thread thread = client.beta().threads().create();

        client.beta().threads().messages().create(MessageCreateParams.builder()
                .threadId(thread.id())
                .role(MessageCreateParams.Role.USER)
                .content(prompt)
                .build());

        Run run = client.beta().threads().runs().create(RunCreateParams.builder()
                .threadId(thread.id())
                .assistantId(assistantId)
                .build());

        int maxRetries = 100;
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

                    String result = dispatcher.handleToolCall(functionName, argumentsJson);

                    toolOutputs.add(RunSubmitToolOutputsParams.ToolOutput.builder()
                            .toolCallId(toolCall.id())
                            .output(result)
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
                        .threadId(thread.id())
                        .build())
                .data();
        return messages.stream()
                .filter(m -> m.role().equals("assistant"))
                .map(Message::content)
                .map(Object::toString)
                .reduce("", (a, b) -> a + "\n" + b).trim();
    }
}
