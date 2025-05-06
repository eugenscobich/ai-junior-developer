package ai.junior.developer.controller;

import ai.junior.developer.service.GitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import lombok.AllArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Git Operations", description = "Endpoints to manage Git repository in workspace")
@RestController
@RequestMapping("/api/git")
@AllArgsConstructor
public class GitController {

    private final GitService gitService;

    @Operation(
        operationId = "cloneRepository",
        summary = "Clone or checkout a Git repository into the workspace",
        responses = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
        })
    @PostMapping("/clone")
    public void cloneRepository(
        @Parameter(name = "repoUrl", description = "Git repository url") @RequestParam("repoUrl") String repoUrl
    ) throws GitAPIException, IOException, URISyntaxException {
        gitService.cloneRepository(repoUrl);
    }

    @Operation(summary = "Add files to Git staging area")
    @PostMapping("/add")
    public String addFiles(@RequestParam(required = false) String pattern) throws GitAPIException, IOException {
        gitService.addFiles(pattern);
        return "Files added to staging";
    }

    @Operation(summary = "Commit changes to Git")
    @PostMapping("/commit")
    public String commit(@RequestParam String message) throws GitAPIException, IOException {
        gitService.commit(message);
        return "Changes committed with message: " + message;
    }


    @Operation(summary = "Push changes to remote repository")
    @PostMapping("/push")
    public String push() throws GitAPIException, IOException {
        gitService.push();
        return "Changes pushed to remote";
    }


    @Operation(summary = "Create a new Git branch")
    @PostMapping("/branch")
    public String createBranch(@RequestParam String branchName) throws GitAPIException, IOException {
        gitService.createBranch(branchName);
        return "Branch created and switched to: " + branchName;
    }

    @Operation(summary = "Create a new Git pull request")
    @PostMapping("/pr")
    public String createPullRequest(@RequestParam String title, @RequestParam String description) throws IOException, InterruptedException {
        githubService.createPullRequest(title, description);
        return "Pull request created.";
    }

    @Operation(summary = "Get Git pull request number by branch name")
    @GetMapping("/pr/id")
    public Integer getPullRequestNumberByBranchName(@RequestParam String owner, @RequestParam String repo,
                                    @RequestParam String branchName, @RequestParam String apiToken) throws IOException, InterruptedException {
        return githubService.getPullRequestNumberByBranchName(owner, repo, branchName, apiToken);
    }

    @Operation(summary = "Read comments from Git pull request")
    @GetMapping("/pr/comments")
    public List<String> getComments(@RequestParam String owner, @RequestParam String repo,
                                   @RequestParam Integer pullNumber, @RequestParam String apiToken) throws IOException, InterruptedException {
        return githubService.getComments(owner, repo, pullNumber, apiToken);
    }
}
