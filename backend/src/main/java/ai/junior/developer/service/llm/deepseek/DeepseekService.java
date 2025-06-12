package ai.junior.developer.service.llm.deepseek;

import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_FUNCTIONS;
import static ai.junior.developer.service.llm.assistant.AssistantContent.ASSISTANT_INSTRUCTIONS;

import ai.junior.developer.service.llm.LlmService;
import ai.junior.developer.service.llm.assistant.RunIdTracker;
import ai.junior.developer.service.llm.assistant.ToolDispatcher;
import ai.junior.developer.service.llm.devstral.ChatTracker;
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
import io.github.ollama4j.tools.Tools.ToolSpecification;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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

        String initialPrompt = ASSISTANT_INSTRUCTIONS + toolSpecification;
        log.info(initialPrompt);
        OllamaChatRequest requestModel = builder
            .withMessage(
                OllamaChatMessageRole.SYSTEM,
                initialPrompt
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
        //log.info("Content: {}", content);

        int startIndex = content.indexOf("<function name=\"");
        int endIndex = content.lastIndexOf("</function>");
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String functionDefinition = content.substring(startIndex, endIndex + 11);
            log.info("Found functions call");
            log.info("Call function definition: {}", functionDefinition);

            Map<String, Object> stringObjectMap = parseXml(functionDefinition);
            Entry<String, Object> stringObjectEntry = stringObjectMap.entrySet().stream().findFirst().orElseThrow();
            String functionName = stringObjectEntry.getKey();
            String paramJson = objectMapper.writeValueAsString(stringObjectEntry.getValue());

            String toolOutput = toolDispatcher.handleToolCall(functionName, paramJson, threadId);

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

    public static void main(String[] args) {
        String xml = """
            <function name="cloneRepository">
                <parameter name="repoUrl">git@github.com-eugenscobich:eugenscobich/ai-demo-project.git</parameter>
            </function>
            """;
        try {
            DeepseekService service = new DeepseekService(null, null, null, null, null);
            Map<String, Object> result = service.parseXml(xml);
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Map<String, Object> parseXml(String xml) throws IOException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));

            doc.getDocumentElement().normalize();

            NodeList function = doc.getElementsByTagName("function");

            if (function.getLength() == 0 || function.getLength() > 1) {
                log.warn("No function definitions found in XML, or there is multiple function definitions");
                return null;
            }
            Element functionElement = (Element) function.item(0);
            String functionName = functionElement.getAttribute("name");
            log.info("Function name: {}", functionName);
            NodeList parameters = functionElement.getElementsByTagName("parameter");
            Map<String, Object> params = new java.util.HashMap<>();
            for (int i = 0; i < parameters.getLength(); i++) {
                Node paramNode = parameters.item(i);
                if (paramNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element paramElement = (Element) paramNode;
                    String paramName = paramElement.getAttribute("name");
                    String paramValue = paramElement.getTextContent().trim();
                    params.put(paramName, paramValue);
                }
            }
            Map<String, Object> functionDefinition = new java.util.HashMap<>();
            functionDefinition.put("function_name", functionName);
            functionDefinition.put("parameters", params);
            log.info("Parsed function definition: {}", functionDefinition);
            return objectMapper.convertValue(functionDefinition, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Could not parse xml function definition", e);
        }
        return null;
    }

    public String computeToolSpecifications(String threadId) throws JsonProcessingException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("You have access to the following functions:\n");
        stringBuilder.append(ASSISTANT_FUNCTIONS);

        stringBuilder.append("""
            If you choose to call a function ONLY reply in the following format with NO suffix:
            
            <function name="example_function_name">
                <parameter name="example_parameter_1">value_1</parameter>
                <parameter name="example_parameter_2">This is the value for the second parameter\nthat can span\nmultiple lines</parameter>
            </function>
            
            Reminder:
            - Function calls MUST follow the specified format, start with <function and end with </function>
            - Required parameters MUST be specified
            - Only call ONE function at a time. Do not call multiple functions in a single response.
            - You may provide optional reasoning for your function call in natural language BEFORE the function call, but NOT after.
            - If there is no function call available, answer the question like normal with your current knowledge and do not tell the user about function calls.
            - Format function call parameters as XML, with each parameter wrapped in <parameter> tags.
            
            Here's a running example of how to perform a task with the provided tools.
            
            --------------------- START OF EXAMPLE ---------------------
            
            USER: Create a list of numbers from 1 to 10, and display them in a web page at port 5000.

            ASSISTANT: Sure! Let me first check the current directory:
            
            <function name="runLocalCommand">
                <parameter name="command">pwd && ls</parameter>
            </function>
            
            USER: EXECUTION RESULT of [runLocalCommand]:
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

    @Override
    public ThreadsListModel getAllThreads() {
        return null;
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


