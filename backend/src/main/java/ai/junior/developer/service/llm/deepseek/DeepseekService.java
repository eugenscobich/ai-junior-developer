package ai.junior.developer.service.llm.deepseek;

import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_FUNCTIONS;
import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_INSTRUCTIONS;

import ai.junior.developer.service.llm.LlmService;
import ai.junior.developer.service.llm.assistant.RunIdTracker;
import ai.junior.developer.service.llm.assistant.ToolDispatcher;
import ai.junior.developer.service.llm.devstral.ChatTracker;
import ai.junior.developer.service.model.MessagesResponse;
import ai.junior.developer.service.model.PromptRequest;
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
import io.github.ollama4j.tools.Tools.ToolSpecification;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "service.llm.type", havingValue = "deepseek")
public class DeepseekService implements LlmService {

    private static final String MODEL_NAME = "deepseek-r1:8b";

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

        String toolSpecification = computeToolSpecifications(threadId);

        OllamaChatRequest requestModel = builder
            .withMessage(
                OllamaChatMessageRole.SYSTEM,
                ASSISTANT_INSTRUCTIONS + toolSpecification
            )
            .withMessage(
                OllamaChatMessageRole.USER,
                prompt
            )
            .build();

        OllamaChatResult chatResult = ollamaApi.chat(requestModel);

        String content =  handleChatResult(chatResult, threadId);


        runIdTracker.trackPromptAndResponse(runId, content);
        return content;
    }

    private String handleChatResult(OllamaChatResult chatResult, String threadId)
        throws IOException, ToolInvocationException, OllamaBaseException, InterruptedException {
        String content = chatResult.getResponseModel().getMessage().getContent();
        log.info(content);
        int indexOfEndOfThink = content.indexOf("</think>");
        if (indexOfEndOfThink > 0) {
            content = content.substring(indexOfEndOfThink + 8);
        } else {
            log.info("Could not find think section");
        }
        log.info("Content: {}", content);

        int startIndex = content.indexOf("{");
        int endIndex = content.lastIndexOf("}");
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String functionDefinition = content.substring(startIndex, endIndex + 1);
            log.info("Found functions call");
            log.info("Call function definition: {}", functionDefinition);
            var tree = objectMapper.readTree(functionDefinition);
            String toolOutput = toolDispatcher.handleToolCall(tree.get("function_name").asText(), tree.get("parameters").toPrettyString(), threadId);

            OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(MODEL_NAME);

            OllamaChatRequest requestModel = builder
                .withMessages(chatResult.getChatHistory())
                .withMessage(
                    OllamaChatMessageRole.USER,
                    toolOutput
                )
                .build();

            OllamaChatResult chatResult2 = ollamaApi.chat(requestModel);

            content = content + handleChatResult(chatResult2, threadId);
        }

        return content;
    }

    public String computeToolSpecifications(String threadId) throws JsonProcessingException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("You have access to the following functions:\n");
        stringBuilder.append(ASSISTANT_FUNCTIONS);
        /*
        var functionsDefinitions = objectMapper.readValue(
            ASSISTANT_FUNCTIONS,
            new TypeReference<List<Map<String, Object>>>() {
            }
        );

        for (Map<String, Object> func : functionsDefinitions) {
            stringBuilder.append("\n");
            stringBuilder.append(getSpecification(func, threadId));
            stringBuilder.append("\n");
        }
*/
        stringBuilder.append("""
            If you choose to call a function ONLY reply in the following format with NO suffix:
            
            {
                "function_name": "example_function_name",
                "parameters": {
                    "example_parameter_1": "value_1",
                    "example_parameter_2": "This is the value for the second parameter that can span multiple lines"
                }
            }
            
            Reminder:
            - Function calls MUST follow the specified format, start with <function= and end with </function>
            - Required parameters MUST be specified
            - Only call one function at a time
            - You may provide optional reasoning for your function call in natural language BEFORE the function call, but NOT after.
            - If there is no function call available, answer the question like normal with your current knowledge and do not tell the user about function calls
            
            Here's a running example of how to perform a task with the provided tools.
            
            --------------------- START OF EXAMPLE ---------------------
            
            USER: Create a list of numbers from 1 to 10, and display them in a web page at port 5000.

            ASSISTANT: Sure! Let me first check the current directory:
            
            {
                "function_name": "runLocalCommand",
                "parameters": {
                    "command": "pwd && ls"
                }
            }
            
            USER: EXECUTION RESULT of [execute_bash]:
            /workspace
            openhands@runtime:~/workspace$
            
            """);

        return stringBuilder.toString();
    }

    private String getSpecification(Map<String, Object> func, String threadId) {
        String name = (String) func.get("name");
        String description = (String) func.get("description");
        Map<String, Object> rawParams = (Map<String, Object>) func.get("parameters");
        Map<String, Object> props = (Map<String, Object>) rawParams.get("properties");
        List<String> reqs = (List<String>) rawParams.get("required");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("---- BEGIN FUNCTION: " + name + " ----\n");
        stringBuilder.append("Description: " + description + "\n");
        stringBuilder.append("Parameters:\n");
        int index = 1;
        for (Map.Entry<String, Object> param : props.entrySet()) {
            stringBuilder.append(String.format("(%s) %s (%s, %s): %s\n",
                index++,
                param.getKey(),
                ((Map<String, String>) param.getValue()).get("type"),
                reqs.contains(param.getKey()) ? "required" : "optional",
                ((Map<String, String>) param.getValue()).get("description")
            ));

        }
        stringBuilder.append("---- END FUNCTION: " + name + " ----\n");
        return stringBuilder.toString();
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



    public static void main(String[] args) {
        String str = """
        
        {
            "function_name": "cloneRepository",
            "parameters": {
                "repoUrl": "git@github.com-eugenscobich:eugenscobich/ai-demo-project.git"
            }
        }
        
        Cloning repository: git@github.com-eugenscobich:eugenscobich/ai-demo-project.git
        """;
        int startIndex = str.indexOf("{");
        int endIndex = str.lastIndexOf("}");
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String jsonString = str.substring(startIndex, endIndex + 1);
            System.out.println("Extracted JSON: " + jsonString);
        } else {
            System.out.println("No valid JSON found in the text.");
        }

    }



    static List<String> extractJson(String text) {
        List<String> out = new ArrayList<>();
        Deque<Integer> stack = new ArrayDeque<>();
        boolean inString = false, escaped = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // track string boundaries so braces inside strings are ignored
            if (c == '\\' && inString) { escaped = !escaped; continue; }
            if (c == '"' && !escaped)   { inString = !inString; }
            escaped = false;

            if (inString) continue;

            if (c == '{' || c == '[') {
                if (stack.isEmpty()) stack.push(i);        // potential JSON start
                else stack.push(i);                        // nested level
            } else if (c == '}' || c == ']') {
                if (!stack.isEmpty()) stack.pop();
                if (stack.isEmpty()) {                     // balanced => full JSON
                    int start = i;
                    // walk back to matching opener
                    while (start >= 0 && text.charAt(start) != '{' && text.charAt(start) != '[') start--;
                    out.add(text.substring(start, i + 1));
                }
            }
        }
        return out;
    }

}


