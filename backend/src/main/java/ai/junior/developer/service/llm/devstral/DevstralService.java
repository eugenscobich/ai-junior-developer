package ai.junior.developer.service.llm.devstral;

import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_FUNCTIONS;
import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_INSTRUCTIONS;

import ai.junior.developer.service.llm.LlmService;
import ai.junior.developer.service.llm.assistant.RunIdTracker;
import ai.junior.developer.service.llm.assistant.ToolDispatcher;
import ai.junior.developer.service.model.MessagesResponse;
import ai.junior.developer.service.model.PromptRequest;
import ai.junior.developer.service.model.ThreadsListModel;
import ai.junior.developer.service.model.ThreadsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.exceptions.ToolInvocationException;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.tools.Tools;
import io.github.ollama4j.tools.Tools.ToolSpecification;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "service.llm.type", havingValue = "devstral")
public class DevstralService implements LlmService {

    private static final String MODEL_NAME = "devstral:24b";

    private final OllamaAPI ollamaApi;
    private final ObjectMapper objectMapper;
    private final ChatTracker chatTracker;
    private final ToolDispatcher toolDispatcher;
    private final RunIdTracker runIdTracker;

    @Override
    @SneakyThrows
    public String startAThread() {
        var threadId = UUID.randomUUID().toString();
        chatTracker.addAThread(threadId);
        MDC.put("threadId", threadId);
        log.info("Thread id: {}", threadId);
        return threadId;
    }

    @Override
    public void continueAThread(String threadId) {
        MDC.put("threadId", threadId);
    }

    @Override
    @SneakyThrows
    public String executeLlmPrompt(String prompt, String threadId) {
        var runId = UUID.randomUUID().toString();
        runIdTracker.track(runId, prompt);
        MDC.put("runId", runId);
        log.info("Run id: {}", runId);

        OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(MODEL_NAME);

        List<ToolSpecification> toolSpecifications = computeToolSpecifications(threadId);
        ollamaApi.registerTools(toolSpecifications);

        OllamaChatRequest requestModel = builder
            .withMessage(
                OllamaChatMessageRole.SYSTEM,
                ASSISTANT_INSTRUCTIONS
            )
            .withMessage(
                OllamaChatMessageRole.USER,
                prompt
            )
            .build();

        OllamaChatResult chatResult = ollamaApi.chat(requestModel);

        String content =  handleCharResult(chatResult, threadId);


        runIdTracker.trackPromptAndResponse(runId, content);
        return content;
    }

    private String handleCharResult(OllamaChatResult chatResult, String threadId)
        throws IOException, ToolInvocationException, OllamaBaseException, InterruptedException {
        if ("stop".equals(chatResult.getResponseModel().getDoneReason())) {
            log.info(chatResult.getResponseModel().getMessage().getContent());
            log.info("Let's continue if done reason is stop");
            OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(MODEL_NAME);

            List<ToolSpecification> toolSpecifications = computeToolSpecifications(threadId);
            ollamaApi.registerTools(toolSpecifications);
            OllamaChatRequest requestModel = builder
                .withMessages(chatResult.getChatHistory())
                .withMessage(
                    OllamaChatMessageRole.USER,
                    "continue"
                )
                .build();
            OllamaChatResult chatResult2 = ollamaApi.chat(requestModel);
            return handleCharResult(chatResult2, threadId);
        }

        return chatResult.getResponseModel().getMessage().getContent();
    }

    public List<Tools.ToolSpecification> computeToolSpecifications(String threadId) throws JsonProcessingException {

        List<Tools.ToolSpecification> toolSpecifications = new ArrayList<>();

        var functionsDefinitions = objectMapper.readValue(
            ASSISTANT_FUNCTIONS,
            new TypeReference<List<Map<String, Object>>>() {
            }
        );

        for (Map<String, Object> func : functionsDefinitions) {
            String name = (String) func.get("name");
            //log.info("Compute function: {}", name);
            var spec = getSpecification(func, threadId);
            toolSpecifications.add(spec);
        }

        return toolSpecifications;
    }

    private Tools.ToolSpecification getSpecification(Map<String, Object> func, String threadId) {
        String name = (String) func.get("name");
        String description = (String) func.get("description");
        Map<String, Object> rawParams = (Map<String, Object>) func.get("parameters");
        Map<String, Object> props = (Map<String, Object>) rawParams.get("properties");
        List<String> reqs = (List<String>) rawParams.get("required");

        return Tools.ToolSpecification.builder()
            .functionName(name)
            .functionDescription(description)
            .toolFunction(args -> {
                try {
                    return toolDispatcher.handleToolCall(name, objectMapper.writeValueAsString(args), threadId);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            })
            .toolPrompt(
                Tools.PromptFuncDefinition.builder()
                    .type("prompt")
                    .function(
                        Tools.PromptFuncDefinition.PromptFuncSpec.builder()
                            .name(name)
                            .description(description)
                            .parameters(
                                Tools.PromptFuncDefinition.Parameters.builder()
                                    .type("object")
                                    .properties(
                                        props.entrySet().stream()
                                            .collect(
                                                java.util.stream.Collectors.toMap(
                                                    Map.Entry::getKey,
                                                    entry -> {
                                                        Map<String, Object> map = (Map<String, Object>) entry.getValue();
                                                        return Tools.PromptFuncDefinition.Property.builder()
                                                            .type(map.get("type").toString())
                                                            .description(map.get("description").toString())
                                                            .required(reqs.contains(entry.getKey()))
                                                            .build();
                                                    }
                                                )
                                            )
                                    )
                                    .required(reqs)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();
    }

    @Override
    public MessagesResponse getThreadMessages(String threadId) {
        return null;
    }

    @Override
    public Map<String, String> sendPromptToExistingThread(PromptRequest request) {
        return null;
    }

    @Override
    public ThreadsResponse getLastThread() {
        return null;
    }

    @Override
    public ThreadsListModel getAllThreads() {
        return null;
    }
}
