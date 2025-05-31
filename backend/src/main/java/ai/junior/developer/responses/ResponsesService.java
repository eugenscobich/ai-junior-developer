package ai.junior.developer.responses;

import ai.junior.developer.assistant.ToolDispatcher;
import ai.junior.developer.service.model.ResponsesByRoleModel;
import ai.junior.developer.service.model.ResponsesItemModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.responses.*;
import com.openai.models.responses.inputitems.InputItemListPage;
import com.openai.models.responses.inputitems.InputItemListParams;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static ai.junior.developer.assistant.AssistantContent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResponsesService {

    private final OpenAIClient client;
    private final ToolDispatcher dispatcher;
    private final ResponseIdTracker responseIdTracker;

    public Response createResponses(
            String userContent,
            @Nullable String previousResponseId
    ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> functions = mapper.readValue(
                RESPONSES_FUNCTIONS,
                new TypeReference<>() {
                }
        );

        List<Tool> toolDefs = new ArrayList<>();
        for (Map<String, Object> func : functions) {
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

        String hash = hashInstructions(ASSISTANT_INSTRUCTIONS);
        Map<String, JsonValue> metadataMap = Map.of(
                "instructionHash", JsonValue.from(hash)
        );

        Response firstResponse = client.responses().create(
                ResponseCreateParams.builder()
                        .model(ASSISTANT_MODEL)
                        .tools(toolDefs)
                        .input(ResponseCreateParams.Input.ofText(userContent))
                        .metadata(
                                ResponseCreateParams.Metadata.builder()
                                        .additionalProperties(metadataMap)
                                        .build()
                        )
                        .build()
        );

        Optional<ResponseOutputItem> functionCall = firstResponse.output().stream()
                .filter(ResponseOutputItem::isFunctionCall)
                .findFirst();

        if (functionCall.isEmpty()) {
            responseIdTracker.track(firstResponse.id());
            return firstResponse;
        }

        ResponseOutputItem fnCall = functionCall.get();
        String callId = fnCall.asFunctionCall().callId();
        String fnName = fnCall.asFunctionCall().name();
        String argJson = fnCall.asFunctionCall().arguments();

        String uuid = UUID.randomUUID().toString().replace("-", "");
        String toolResultJson = dispatcher.handleToolCall(fnName, argJson, uuid);
        List<ResponseInputItem> followUpInputs = new ArrayList<>();

        ResponseFunctionToolCall fnCallItem = ResponseFunctionToolCall.builder()
                .name(fnName)
                .arguments(argJson)
                .callId(callId)
                .build();

        followUpInputs.add(ResponseInputItem.ofFunctionCall(fnCallItem));
        ResponseInputItem.FunctionCallOutput fnOutputItem = ResponseInputItem.FunctionCallOutput.builder()
                .callId(callId)
                .output(toolResultJson)
                .build();

        followUpInputs.add(ResponseInputItem.ofFunctionCallOutput(fnOutputItem));
        Response secondResponse = client.responses().create(
                ResponseCreateParams.builder()
                        .model(ASSISTANT_MODEL)
                        .tools(toolDefs)
                        .input(ResponseCreateParams.Input.ofResponse(followUpInputs))
                        .previousResponseId(firstResponse.id())
                        .build()
        );

        responseIdTracker.track(secondResponse.id());
        return secondResponse;
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
        for (ResponseOutputItem item : outputItems) {
            outputMessages.add(ResponsesItemModel.builder()
                    .messageId(item.asMessage().id())
                    .message(item.asMessage().content().getFirst().asOutputText().text())
                    .build());
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
}
