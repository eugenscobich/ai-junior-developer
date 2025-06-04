package ai.junior.developer.service.llm.responses;

import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_INSTRUCTIONS;
import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_MODEL;
import static ai.junior.developer.service.llm.assistant.AssistantContent.RESPONSES_FUNCTIONS;

import ai.junior.developer.service.llm.LlmService;
import ai.junior.developer.service.llm.assistant.ToolDispatcher;
import ai.junior.developer.service.model.ResponsesByRoleModel;
import ai.junior.developer.service.model.ResponsesItemModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.responses.FunctionTool;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseCreateParams.Metadata;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseFunctionToolCallOutputItem;
import com.openai.models.responses.ResponseInputContent;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseInputText;
import com.openai.models.responses.ResponseItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseReasoningItem.Summary;
import com.openai.models.responses.ResponseRetrieveParams;
import com.openai.models.responses.Tool;
import com.openai.models.responses.inputitems.InputItemListPage;
import com.openai.models.responses.inputitems.InputItemListParams;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    public ResponsesByRoleModel getInputListMessages(String responseId) throws IOException {
        InputItemListPage page = client.responses()
            .inputItems()
            .list(InputItemListParams.builder()
                .responseId(responseId)
                .build());
        log.info("inputItemService: " + page.data().toString());
        List<ResponsesItemModel> userMessages = new ArrayList<>();
        List<ResponsesItemModel> assistantMessages = new ArrayList<>();
        List<ResponsesItemModel> functionCall = new ArrayList<>();

        for (ResponseItem item : page.data()) {
            if (item.isResponseInputMessageItem()) {
                for (ResponseInputContent segment : item.asResponseInputMessageItem().content()) {
                    if ("input_text".equals(segment.asInputText()._type().toString())) {
                        ResponseInputText txt = segment.asInputText();
                        userMessages.add(
                            ResponsesItemModel.builder()
                                .messageId(item.asResponseInputMessageItem().id())
                                .message(txt.text())
                                .type("user")
                                .build()
                        );
                    }
                }
                continue;
            }

            if (item.isResponseOutputMessage()) {
                for (ResponseOutputMessage.Content segment : item.asResponseOutputMessage().content()) {
                    if ("output_text".equals(segment.asOutputText()._type().toString())) {
                        String text = segment.asOutputText().text();
                        assistantMessages.add(
                            ResponsesItemModel.builder()
                                .messageId(item.asResponseOutputMessage().id())
                                .message(text)
                                .type("assistant")
                                .build()
                        );
                    }
                }
            }

            if (item.isFunctionCallOutput()) {
                ResponseFunctionToolCallOutputItem fnOut = item.asFunctionCallOutput();
                String text = fnOut.output();
                functionCall.add(
                    ResponsesItemModel.builder()
                        .messageId(fnOut.id())
                        .type("functionToolCall")
                        .message(text)
                        .build()
                );
            }
        }

        return ResponsesByRoleModel.builder()
            .user(userMessages)
            .assistant(assistantMessages)
            .functionCall(functionCall)
            .build();
    }

    public List<ResponsesItemModel> getOutputListMessages(String responseId) {
        var outputResponse = client.responses().retrieve(
            ResponseRetrieveParams.builder()
                .responseId(responseId)
                .build());
        log.info("getOutputListMessages: " + outputResponse.output());
        List<ResponseOutputItem> outputItems = outputResponse.output();
        List<ResponsesItemModel> outputMessages = new ArrayList<>();
        for (ResponseOutputItem item : outputResponse.output()) {
            if (item.isMessage()) {
                ResponseOutputMessage msg = item.asMessage();
                String text = msg.content().getFirst().asOutputText().text();
                outputMessages.add(
                    ResponsesItemModel.builder()
                        .messageId(msg.id())
                        .message(text)
                        .type("assistant")
                        .build()
                );
                continue;
            }

            if (item.isFunctionCall()) {
                ResponseFunctionToolCall fnCall = item.asFunctionCall();
                String text = "FUNCTION CALL → " + fnCall.name() + "(args=" + fnCall.arguments() + ")";
                outputMessages.add(
                    ResponsesItemModel.builder()
                        .messageId(fnCall.id().orElse("<no-id>"))
                        .message(text)
                        .type("functionToolCall")
                        .build()
                );
            }
        }

        return outputMessages;
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

    public Map<String, String> getAssistantMessage(String responseId) throws Exception {
        Map<String, String> responses = new HashMap<>();
        ResponsesByRoleModel trackedUser = getInputListMessages(responseId);
        List<ResponsesItemModel> trackedAssistant = getOutputListMessages(responseId);

        String fullAssistantMsgId = trackedAssistant.get(0).getMessageId();
        String assistantMessageIdExtract = StringUtils.left(
            StringUtils.substringAfter(fullAssistantMsgId, "_"), 6);

        boolean hasFunctionCallTool = trackedAssistant.stream()
            .anyMatch(item -> "functionToolCall".equals(item.getType()));
        Stream<ResponsesItemModel> chosenStream;
        if (hasFunctionCallTool) {
            chosenStream = trackedUser.getFunctionCall().stream();
        } else {
            chosenStream = trackedAssistant.stream()
                .filter(item -> "assistant".equals(item.getType()));
        }

        responses.put("runId", assistantMessageIdExtract);
        responses.put("assistantMessage", chosenStream.map(ResponsesItemModel::getMessage).findAny().get());
        return responses;
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

    public String startAThread() {
        var threadId = UUID.randomUUID().toString();
        responsesTracker.addAThread(threadId);
        return threadId;
    }

    @Override
    public String executePrompt(String prompt, String threadId) {
        var previousResponsesId = responsesTracker.getLastTrackedResponseId(threadId);
        var metadata = getMetadata(threadId);
        var llmResponse = new StringBuilder();
        final Response response;
        if (previousResponsesId == null) {
            log.info("Strat new conversation for thread id: {}", threadId);
            var toolDefinitions = getToolDefinitions();
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
                    .previousResponseId(previousResponsesId)
                    .input(ResponseCreateParams.Input.ofText(prompt))
                    .metadata(metadata)
                    .build()
            );
        }
        responsesTracker.track(threadId, response.id());

        handleResponseOutput(threadId, response, llmResponse);

        return llmResponse.toString();
    }

    private void handleResponseOutput(
        String threadId,
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
                log.info("Call id: {}", callId);
                var fnName = responseOutput.asFunctionCall().name();
                var argJson = responseOutput.asFunctionCall().arguments();
                var toolResultJson = dispatcher.handleToolCall(fnName, argJson, threadId);

                log.info("Tool result [{}]", toolResultJson);
                followUpInputs.add(ResponseInputItem.ofFunctionCall(responseOutput.asFunctionCall()));
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
            responsesTracker.track(threadId, submitFunctionsResponse.id());
            handleResponseOutput(threadId, submitFunctionsResponse, llmResponse);
        }
    }

    @NotNull
    private static Metadata getMetadata(String threadId) {
        Map<String, JsonValue> metadataMap = Map.of(
            "threadId", JsonValue.from(threadId)
        );
        return Metadata.builder()
            .additionalProperties(metadataMap)
            .build();
    }

    @Override
    public void continueAThread(String threadId) {
        MDC.put("threadId", threadId);
    }
}
