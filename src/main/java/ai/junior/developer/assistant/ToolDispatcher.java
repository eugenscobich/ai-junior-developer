package ai.junior.developer.assistant;

import ai.junior.developer.controller.FilesController;
import ai.junior.developer.controller.GitController;
import ai.junior.developer.service.FilesService;
import ai.junior.developer.service.GitService;
import ai.junior.developer.service.MavenService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ToolDispatcher {

    private final MavenService mavenService;
    private final FilesService filesService;
    private final GitService gitService;
    private final ObjectMapper mapper = new ObjectMapper();

    public String handleToolCall(String functionName, String argsJson) {
        try {
            Map<String, Object> args = mapper.readValue(argsJson, new TypeReference<>() {
            });

            return switch (functionName) {
                case "listFiles" -> String.join("\n", filesService.listFiles());

                case "readFile" -> {
                    String filePath = (String) args.get("filePath");
                    yield filesService.readFile(filePath);
                }

                case "writeFile" -> {
                    String filePath = (String) args.get("filePath");
                    String content = (String) args.get("fileContent");
                    filesService.writeFile(filePath, content);
                    yield "File written successfully: " + filePath;
                }

                case "replaceInFile" -> {
                    String filePath = (String) args.get("filePath");
                    String from = (String) args.get("from");
                    String to = (String) args.get("to");
                    filesService.replaceInFile(filePath, from, to);
                    yield "File content replaced successfully: " + filePath;
                }

                case "cloneRepository" -> {
                    gitService.cloneRepository((String) args.get("repoUrl"));
                    yield "Repository cloned: " + args.get("repoUrl");
                }

                case "createBranch" -> {
                    gitService.createBranch((String) args.get("branchName"));
                    yield "Branch created: " + args.get("branchName");
                }

                case "addFiles" -> {
                    gitService.addFiles((String) args.getOrDefault("pattern", null));
                    yield "Files added to staging.";
                }

                case "commit" -> {
                    gitService.commit((String) args.get("message"));
                    yield "Committed with message: " + args.get("message");
                }

                case "push" -> {
                    gitService.push();
                    yield "Changes pushed.";
                }

                case "createPullRequest" -> {
                    gitService.createPullRequest(
                            (String) args.get("owner"),
                            (String) args.get("repo"),
                            (String) args.get("branchName"),
                            (String) args.get("apiToken")
                    );
                    yield "Pull request created.";
                }

                case "getPullRequestNumberByBranchName" -> {
                    Integer prNumber = gitService.getPullRequestNumberByBranchName(
                            (String) args.get("owner"),
                            (String) args.get("repo"),
                            (String) args.get("branchName"),
                            (String) args.get("apiToken")
                    );
                    yield "Pull request number: " + prNumber;
                }

                case "getComments" -> {
                    List<String> comments = gitService.getComments(
                            (String) args.get("owner"),
                            (String) args.get("repo"),
                            (Integer) args.get("pullNumber"),
                            (String) args.get("apiToken")
                    );
                    yield String.join("\n", comments);
                }

                case "runCleanInstall" -> mavenService.runCleanInstall((String) args.get("project"));

                case "runTests" -> mavenService.runTests((String) args.get("project"));

                default -> "Unknown function: " + functionName;
            };
        } catch (Exception e) {
            return "Error executing function " + functionName + ": " + e.getMessage();
        }
    }
}
