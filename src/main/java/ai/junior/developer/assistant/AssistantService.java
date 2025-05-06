package ai.junior.developer.assistant;

import static com.openai.models.beta.threads.messages.Message.Role.Value.ASSISTANT;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.core.http.QueryParams;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.beta.assistants.Assistant;
import com.openai.models.beta.assistants.AssistantCreateParams;
import com.openai.models.beta.assistants.AssistantDeleteParams;
import com.openai.models.beta.assistants.AssistantListPage;
import com.openai.models.beta.assistants.AssistantListParams;
import com.openai.models.beta.threads.Thread;
import com.openai.models.beta.threads.messages.Message;
import com.openai.models.beta.threads.messages.MessageContent;
import com.openai.models.beta.threads.messages.MessageCreateParams;
import com.openai.models.beta.threads.messages.MessageListParams;
import com.openai.models.beta.threads.messages.Text;
import com.openai.models.beta.threads.messages.TextContentBlock;
import com.openai.models.beta.threads.runs.RequiredActionFunctionToolCall;
import com.openai.models.beta.threads.runs.Run;
import com.openai.models.beta.threads.runs.RunCreateParams;
import com.openai.models.beta.threads.runs.RunRetrieveParams;
import com.openai.models.beta.threads.runs.RunStatus;
import com.openai.models.beta.threads.runs.RunSubmitToolOutputsParams;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
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
            .name("AI Junior Developer")
            .description("Assistant for professional software developers that is able to read and modify your files and perform tasks.")
            .instructions(

                """
                    AI Junior Developer — System Prompt You are AI Junior Developer, a GPT-based assistant that supports software development by interacting with a predefined set of backend API actions.
                    
                    Role and Behavior
                    - You communicate in a concise, technical, and professional tone.
                    - You strictly use the provided actions for Git, file, and build operations — never internal tools.
                    - You follow instructions precisely, validate them for contradictions, and suggest improvements where appropriate.
                    - You follow modern development principles: Clean Code, DRY, KISS, YAGNI, SOLID, TDD, OOP, SoC, POLA. Use functional programming when appropriate and favor stateless, idempotent actions. Avoid code smells. Available Actions (API Mappings)
                    
                    Supported Operations:
                    - cloneRepository: Clone the repo
                    - createBranch: Create git branch
                    - addFiles: Add files to staging
                    - commit: Commit
                    - push: Push to remote
                    - createPullRequest: Create pull requests in GitHub
                    - getPullRequestNumberByBranchName: Get Pull Request Number By Branch Name
                    - getComments: Get PR Comments
                    - runCleanInstall: Run maven clean install
                    - runTests: Run Tests
                    - listFiles: List all project files
                    - readFiles: Read Files
                    - readFile: Read File
                    - writeFile: Overwrites or creates a file 
                    - replaceInFile: Use for inserting, updating, or appending Execution
                    
                    Workflow
                    When the user requests a task:
                    1. Clone the repository
                    - Use cloneRepository operation.
                    - If no URL is provided, ask for it once per session only.
                    2. List all files
                    - Use listFiles.
                    3. Read files
                    - Use readFiles for multiple files, readFile for a single one.
                    - If a file is missing, re-check with listFiles.
                    4. Create a branch
                    - Use createBranch, naming it feature/{ticketNumber}.
                    5. Make changes
                    - Use replaceInFile to modify, append, or insert content.
                    - Use writeFile only for completely new or overwritten files.
                    6. Stage changes
                    - Use addFiles.
                    7. Commit changes
                    - Use commit with a meaningful message.
                    8. Run Maven build
                    - Use runCleanInstall to execute mvn clean install.
                    - If the build fails, read the returned logs in the response.
                    - Identify the cause of failure from the logs.
                    - Use readFiles and replaceInFile to correct issues.
                    - Repeat build and fix cycle until the build succeeds.
                    - Then commit and push the changes.
                    9. Run tests
                      - Only run tests if unit tests were created or updated.
                      - Use runTests to execute Maven tests.
                      - Read logs returned in the response to identify failed tests or issues.
                      - Use readFiles and replaceInFile to fix or improve the test code.
                      - Repeat the runTests and fix process until all tests pass.
                      - Then commit and push the changes.
                    10. Push changes
                    - Use push to remote only after the build is successful.
                    11. Open a pull request
                    - Use createPullRequest to create a PR for the pushed branch.
                    12. Get pull request number
                    - Use getPullRequestNumberByBranchName to retrieve the PR ID.
                    13. Review pull request comments
                    - Use getComments to read PR feedback.
                    - Based on comments, inspect and review the branch for required changes.
                    
                    Important Rules:
                    - Only use the functions listed above — never use internal tools or assumptions such git operations!
                    - Ask once per session if a required input (e.g., repo URL) is missing.
                    - Follow the execution steps in order without skipping.
                    - Clearly explain actions and results using professional language.
                    - Continue to proceed in order to full complete the task
                    
                    """);

        for (FunctionDefinition fn : toolDefs) {
            builder.addFunctionTool(fn);
        }

        AssistantCreateParams params = builder.build();
        Assistant assistant = client.beta().assistants().create(params);
        log.info("AssistantId:" + assistant.id());
        return assistant;
    }

    public void deleteAssistant(String assistantId) {
        client.beta().assistants().delete(AssistantDeleteParams.builder()
            .assistantId(assistantId)
            .build());
    }

    public String executePrompt(String prompt, String assistantId, String threadId) throws Exception {

        client.beta().threads().messages().create(MessageCreateParams.builder()
            .threadId(threadId)
            .role(MessageCreateParams.Role.USER)
            .content(prompt)
            .build());

        Run run = client.beta().threads().runs().create(RunCreateParams.builder()
            .threadId(threadId)
            .assistantId(assistantId)
            .build());
        log.info("Run id: {}", run.id());
        int maxRetries = 1000;
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
                .threadId(threadId)
                .build())
            .data();
        log.info(messages.toString());
        return messages.stream()
            .filter(m -> m.role().value().equals(ASSISTANT))
            .map(Message::content)
            .flatMap(Collection::stream)
            .map(MessageContent::text)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(TextContentBlock::text)
            .map(Text::value)
            .collect(Collectors.joining(" "));
    }

    public Thread createThread() {
        Thread thread = client.beta().threads().create();
        log.info("Thread id: {}", thread.id());
        return thread;
    }

    public Assistant findOrCreateAssistant() throws IOException {
        Assistant assistant = findAssistant();
        if (assistant == null) {
            return createAssistant();
        }
        return assistant;
    }

    public Assistant findAssistant() {
        AssistantListPage list = client.beta().assistants().list();
        return list.response().data().stream()
            .filter(assistant -> assistant.name().isPresent() && assistant.name().get().equals("AI Junior Developer"))
            .findFirst().orElse(null);
    }
}
