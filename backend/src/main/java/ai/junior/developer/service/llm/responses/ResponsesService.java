package ai.junior.developer.service.llm.responses;

import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_FUNCTIONS;
import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_INSTRUCTIONS;
import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_MODEL;

import ai.junior.developer.service.llm.LlmService;
import ai.junior.developer.service.llm.assistant.RunIdTracker;
import ai.junior.developer.service.llm.assistant.ToolDispatcher;
import ai.junior.developer.service.llm.responses.ResponsesTracker.ResponsesThreadTracker;
import ai.junior.developer.service.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.responses.*;
import com.openai.models.responses.ResponseCreateParams.Metadata;
import com.openai.models.responses.ResponseReasoningItem.Summary;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.openai.models.responses.inputitems.InputItemListPage;
import com.openai.models.responses.inputitems.InputItemListParams;
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
    private final RunIdTracker runIdTracker;
    private final ObjectMapper objectMapper;

    public String startAThread() {
        var threadId = UUID.randomUUID().toString();
        responsesTracker.addAThread(threadId);
        MDC.put("threadId", threadId);
        log.info("Thread id: {}", threadId);
        return threadId;
    }

    @Override
    public String executeLlmPrompt(String prompt, String threadId) {
        var runId = UUID.randomUUID().toString();
        runIdTracker.track(runId, prompt);
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
        runIdTracker.trackPromptAndResponse(runId, llmResponse.toString());
        return llmResponse.toString();
    }

    @Override
    public MessagesResponse getThreadMessages(String threadId) {
        List<ResponsesThreadTracker> threadDetails = responsesTracker.getThreadDetails(threadId);
        if (threadDetails == null || threadDetails.isEmpty()) {
            return MessagesResponse.builder().messagesList(List.of()).build();
        }

        List<MessageResponse> allResponses = new ArrayList<>();
        Map<String, ResponsesPromptResponseModel> responsesByRunId = runIdTracker.getResponsesByRunId();

        for (ResponsesThreadTracker tracker : threadDetails) {
            String runId = tracker.getRunId();
            ResponsesPromptResponseModel respModel = responsesByRunId.get(runId);

            if (respModel == null) continue;
            UserOrAssistantMessageResponse userMsgDto = UserOrAssistantMessageResponse.builder()
                    .threadId(threadId)
                    .value(respModel.getUserPrompt())
                    .createdAt(System.currentTimeMillis())
                    .runId(runId)
                    .build();

            UserOrAssistantMessageResponse assistantMsgDto = UserOrAssistantMessageResponse.builder()
                    .threadId(threadId)
                    .value(respModel.getResponse())
                    .createdAt(System.currentTimeMillis())
                    .runId(runId)
                    .build();

            allResponses.add(
                    MessageResponse.builder()
                            .userMessage(userMsgDto)
                            .assistantMessages(List.of(assistantMsgDto))
                            .build()
            );
        }

        return MessagesResponse.builder()
                .messagesList(allResponses)
                .build();
    }

    @Override
    public Map<String, String> sendPromptToExistingThread(PromptRequest request) {
        MDC.put("threadId", request.getThreadId());
        String response = executeLlmPrompt(request.getPrompt(), request.getThreadId());
        Optional<String> runId = runIdTracker.getAllTrackedRunId().keySet().stream().reduce((first, second) -> second);

        return Map.of("runId", runId.get(),
                "assistantMessage", response);
    }

    @Override
    public ThreadsResponse getLastThread() {
        var activeThread = responsesTracker.getLastThreadId();
        return ThreadsResponse.builder()
                .assistantId("assistant")
                .threadId(activeThread)
                .build();
    }

    @Override
    public ThreadsListModel getAllThreads() {
        var threads = responsesTracker.getThreads();
        return ThreadsListModel.builder()
                .threadList(threads)
                .build();
    }

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

    public Map<String, String> getAssistantMessage(String responseId, String threadId) throws Exception {
        Map<String, String> responses = new HashMap<>();
        ResponsesByRoleModel trackedUser = getInputListMessages(responseId);
        List<ResponsesItemModel> trackedAssistant = getOutputListMessages(responseId);

        String lastRunId = responsesTracker.getLastTrackedResponseId(threadId);

        boolean hasFunctionCallTool = trackedAssistant.stream()
                .anyMatch(item -> "functionToolCall".equals(item.getType()));
        Stream<ResponsesItemModel> chosenStream;
        if (hasFunctionCallTool) {
            chosenStream = trackedUser.getFunctionCall().stream();
        } else {
            chosenStream = trackedAssistant.stream()
                    .filter(item -> "assistant".equals(item.getType()));
        }

        responses.put("runId", lastRunId);
        responses.put("assistantMessage", chosenStream.map(ResponsesItemModel::getMessage).findAny().get());
        return responses;
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
                ASSISTANT_FUNCTIONS,
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
