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
import com.openai.models.beta.threads.messages.MessageCreateParams;
import com.openai.models.beta.threads.runs.Run;
import com.openai.models.beta.threads.Thread;
import com.openai.models.beta.threads.runs.RunCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
