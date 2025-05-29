package ai.junior.developer.assistant;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.beta.threads.messages.MessageContent;
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

    public Response createResponses(String userContent, @Nullable String previousResponseId) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, Object>> functions = mapper.readValue(ASSISTANT_FUNCTIONS, new TypeReference<>() {
        });

        List<Tool> toolDefs = new ArrayList<>();

        for (Map<String, Object> func : functions) {
            String name = (String) func.get("name");
            String description = (String) func.get("description");

            Map<String, Object> rawParams = (Map<String, Object>) func.get("parameters");
            Map<String, JsonValue> paramJson = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawParams.entrySet()) {
                paramJson.put(entry.getKey(), JsonValue.from(entry.getValue()));
            }

            FunctionTool.Parameters parameters = FunctionTool.Parameters.builder()
                    .additionalProperties(paramJson)
                    .build();

            toolDefs.add(Tool.ofFunction(FunctionTool.builder()
                    .name(name)
                    .description(description)
                    .parameters(parameters)
                    .build()));
        }

        String hash = hashInstructions(ASSISTANT_INSTRUCTIONS);
        Map<String, JsonValue> metadataMap = Map.of(
                "instructionHash", JsonValue.from(hash)
        );

        var build = ResponseCreateParams.builder().model(ASSISTANT_MODEL)
                .tools(toolDefs)
                .input(ResponseCreateParams.Input.ofText(userContent))
                .metadata(ResponseCreateParams.Metadata.builder()
                        .additionalProperties(metadataMap)
                        .build());
        if (previousResponseId != null) {
            build.previousResponseId(previousResponseId);
        } else {
            build.instructions(ASSISTANT_INSTRUCTIONS);
        }

        var params = build.build();
        return client.responses().create(params);
    }

    public List<String> getListMessages(String responseId) throws IOException {
        var inputItemService = client.responses().inputItems();
        var params = InputItemListParams.builder()
                .responseId(responseId)
                .build();
        InputItemListPage page = inputItemService.list(params);

        List<String> messages = new ArrayList<>();

        for(ResponseItem item: page.data()) {
            if(item.responseOutputMessage().isPresent()) {
                item.responseOutputMessage().map(content -> content.content().stream()
                        .map(str -> messages.add(str.asOutputText().text())));
            }
        }

        return messages;
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
