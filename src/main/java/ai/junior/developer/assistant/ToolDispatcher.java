package ai.junior.developer.assistant;

import ai.junior.developer.service.FilesService;
import ai.junior.developer.service.GitHubService;
import ai.junior.developer.service.GitService;
import ai.junior.developer.service.RunService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToolDispatcher {

    private final RunService runService;
    private final FilesService filesService;
    private final GitHubService githubService;
    private final GitService gitService;
    private final ObjectMapper mapper = new ObjectMapper();

    public String handleToolCall(String functionName, String argsJson, String threadId) {
        try {
            Map<String, Object> args = mapper.readValue(
                argsJson, new TypeReference<>() {
                }
            );

            return switch (functionName) {
                case "listFiles" -> String.join("\n", filesService.listFiles(threadId));

                case "readFile" -> {
                    String filePath = (String) args.get("filePath");
                    yield filesService.readFile(filePath, threadId);
                }

                case "readFiles" -> {
                    List<String> filePaths = (List<String>) args.get("filePaths");
                    yield filesService.readFiles(filePaths, threadId).toString();
                }

                case "writeFile" -> {
                    String filePath = (String) args.get("filePath");
                    String content = (String) args.get("fileContent");
                    filesService.writeFile(filePath, content, threadId);
                    yield "File written successfully: " + filePath;
                }

                case "replaceInFile" -> {
                    String filePath = (String) args.get("filePath");
                    String from = (String) args.get("from");
                    String to = (String) args.get("to");
                    filesService.replaceInFile(filePath, from, to, threadId);
                    yield "File content replaced successfully: " + filePath;
                }

                case "cloneRepository" -> {
                    gitService.cloneRepository((String) args.get("repoUrl"), threadId);
                    yield "Repository cloned: " + args.get("repoUrl");
                }

                case "createBranch" -> {
                    gitService.createBranch((String) args.get("branchName"), threadId);
                    yield "Branch created: " + args.get("branchName");
                }

                case "reset" -> {
                    gitService.resetCurrentBranch(threadId);
                    yield "Current branch was reset";
                }

                case "addFiles" -> {
                    gitService.addFiles((String) args.getOrDefault("pattern", null), threadId);
                    yield "Files added to staging.";
                }

                case "deleteFiles" -> {
                    List<String> filePaths = (List<String>) args.get("filePaths");
                    filesService.deleteFiles(filePaths, threadId);
                    yield "Files was deleted from workspace.";
                }

                case "commit" -> {
                    gitService.commit((String) args.get("message"), threadId);
                    yield "Committed with message: " + args.get("message");
                }

                case "push" -> {
                    gitService.push(threadId);
                    yield "Changes pushed.";
                }

                case "createPullRequest" -> {
                    githubService.createPullRequest(
                        (String) args.get("title"),
                        (String) args.get("description"),
                        threadId
                    );
                    yield "Pull request created.";
                }

                case "runLocalCommand" -> runService.run((String) args.get("command"), threadId);

                default -> "Unknown function: " + functionName;
            };
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "Error executing function " + functionName + ": " + e.getMessage();
        }
    }
}
