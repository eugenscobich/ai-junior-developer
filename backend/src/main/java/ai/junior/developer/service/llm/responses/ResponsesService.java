package ai.junior.developer.service.llm.responses;

import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_INSTRUCTIONS;
import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_MODEL;
import static ai.junior.developer.service.llm.assistant.AssistantContent.RESPONSES_FUNCTIONS;

import ai.junior.developer.service.llm.LlmService;
import ai.junior.developer.service.llm.assistant.ToolDispatcher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.responses.FunctionTool;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseCreateParams.Metadata;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseReasoningItem.Summary;
import com.openai.models.responses.Tool;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "service.llm.type", havingValue = "responses")
public class ResponsesService implements LlmService {

    private final OpenAIClient client;
    private final ToolDispatcher dispatcher;
    private final ResponsesTracker responsesTracker;
    private final ObjectMapper objectMapper;

    public String startAThread() {
        var threadId = UUID.randomUUID().toString();
        responsesTracker.addAThread(threadId);
        MDC.put("threadId", threadId);
        log.info("Thread id: {}", threadId);
        return threadId;
    }

    @Override
    public String executePrompt(String prompt, String threadId) {
        var runId = UUID.randomUUID().toString();
        MDC.put("runId", runId);
        log.info("Run id: {}", runId);
        var previousResponsesId = responsesTracker.getLastTrackedResponseId(threadId);

        var metadata = getMetadata(threadId, runId);
        var llmResponse = new StringBuilder();
        final Response response;
        var toolDefinitions = getToolDefinitions();
        if (previousResponsesId == null) {
            log.info("Strat new conversation for thread id: {}", threadId);
            response = client.responses().create(
                ResponseCreateParams.builder()
                    .model(ASSISTANT_MODEL)
                    .instructions(ASSISTANT_INSTRUCTIONS)
                    .tools(toolDefinitions)
                    .input(ResponseCreateParams.Input.ofText(prompt))
                    .metadata(metadata)
                    .build()
            );
        } else {
            log.info("Continue the conversation for thread id: {} and previous response id: {}", threadId, previousResponsesId);
            response = client.responses().create(
                ResponseCreateParams.builder()
                    .model(ASSISTANT_MODEL)
                    .instructions(ASSISTANT_INSTRUCTIONS)
                    .tools(toolDefinitions)
                    .previousResponseId(previousResponsesId)
                    .input(ResponseCreateParams.Input.ofText(prompt))
                    .metadata(metadata)
                    .build()
            );
        }
        responsesTracker.track(threadId, runId, response.id());
        handleResponseOutput(threadId, runId, response, llmResponse);

        return llmResponse.toString();
    }

    private void handleResponseOutput(
        String threadId,
        String runId,
        Response response,
        StringBuilder llmResponse
    ) {
        var responseOutputItemStream = response.output().stream();
        var followUpInputs = new ArrayList<ResponseInputItem>();
        responseOutputItemStream.forEach(responseOutput -> {
            if (responseOutput.isMessage()) {
                llmResponse.append("\nMessage: \n");
                llmResponse.append(responseOutput.asMessage()
                    .content().stream()
                    .map(c -> {
                        return c.outputText().orElseThrow().text();
                    })
                    .collect(Collectors.joining("\n")));
            } else if (responseOutput.isReasoning()) {
                llmResponse.append("\nReasoning: \n");
                llmResponse.append(responseOutput.asReasoning()
                    .summary()
                    .stream()
                    .map(Summary::text)
                    .collect(Collectors.joining("\n")));
            } else if (responseOutput.isFunctionCall()) {
                var callId = responseOutput.asFunctionCall().callId();
                log.debug("Call id: {}", callId);
                var fnName = responseOutput.asFunctionCall().name();
                var argJson = responseOutput.asFunctionCall().arguments();
                var toolResultJson = dispatcher.handleToolCall(fnName, argJson, threadId);
                var fnCallItem = ResponseFunctionToolCall.builder()
                    .name(fnName)
                    .arguments(argJson)
                    .callId(callId)
                    .build();
                log.debug("Tool result [{}]", toolResultJson);
                followUpInputs.add(ResponseInputItem.ofFunctionCall(fnCallItem));
                var fnOutputItem = ResponseInputItem.FunctionCallOutput.builder()
                    .callId(callId)
                    .output(toolResultJson)
                    .build();

                followUpInputs.add(ResponseInputItem.ofFunctionCallOutput(fnOutputItem));
            }

        });

        if (!followUpInputs.isEmpty()) {
            var toolDefinitions = getToolDefinitions();
            var submitFunctionsResponse = client.responses().create(
                ResponseCreateParams.builder()
                    .model(ASSISTANT_MODEL)
                    .instructions(ASSISTANT_INSTRUCTIONS)
                    .tools(toolDefinitions)
                    .previousResponseId(response.id())
                    .input(ResponseCreateParams.Input.ofResponse(followUpInputs))
                    .build()
            );
            responsesTracker.track(threadId, runId, submitFunctionsResponse.id());
            handleResponseOutput(threadId, runId, submitFunctionsResponse, llmResponse);
        }
    }

    @NotNull
    private static Metadata getMetadata(String threadId, String runId) {
        Map<String, JsonValue> metadataMap = Map.of(
            "threadId", JsonValue.from(threadId),
            "runId", JsonValue.from(runId)
        );
        return Metadata.builder()
            .additionalProperties(metadataMap)
            .build();
    }

    @Override
    public void continueAThread(String threadId) {
        MDC.put("threadId", threadId);
    }

    @NotNull
    @SneakyThrows
    private List<Tool> getToolDefinitions() {
        var functionsDefinitions = objectMapper.readValue(
            RESPONSES_FUNCTIONS,
            new TypeReference<List<Map<String, Object>>>() {
            }
        );

        List<Tool> toolDefs = new ArrayList<>();
        for (Map<String, Object> func : functionsDefinitions) {
            String name = (String) func.get("name");
            String description = (String) func.get("description");

            Map<String, Object> rawParams = (Map<String, Object>) func.get("parameters");
            Map<String, Object> props = (Map<String, Object>) rawParams.get("properties");
            List<String> reqs = (List<String>) rawParams.get("required");

            JsonValue propsJson = JsonValue.from(props);
            JsonValue reqsJson = JsonValue.from(reqs);

            FunctionTool.Parameters parameters = FunctionTool.Parameters.builder()
                .putAdditionalProperty("type", JsonValue.from("object"))
                .putAdditionalProperty("properties", propsJson)
                .putAdditionalProperty("required", reqsJson)
                .putAdditionalProperty("additionalProperties", JsonValue.from(Boolean.FALSE))
                .build();

            toolDefs.add(
                Tool.ofFunction(
                    FunctionTool.builder()
                        .name(name)
                        .description(description)
                        .parameters(parameters)
                        .strict(true)
                        .build()
                )
            );
        }
        return toolDefs;
    }
}
