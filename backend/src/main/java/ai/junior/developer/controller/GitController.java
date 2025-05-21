package ai.junior.developer.controller;

import ai.junior.developer.service.GitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.net.URISyntaxException;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
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
        @Parameter(name = "repoUrl", description = "Git repository url") @RequestParam String repoUrl,
        @Parameter(name = "issueKey", description = "Issue key") @RequestParam String issueKey
    ) throws GitAPIException, IOException, URISyntaxException {
        gitService.cloneRepository(repoUrl, issueKey);
    }

    @Operation(summary = "Add files to Git staging area")
    @PostMapping("/add")
    public String addFiles(
        @Parameter(name = "pattern", description = "Pattern used to add files. List each file individual or the directory path.")
        @RequestParam(required = false) String pattern,
        @Parameter(name = "issueKey", description = "Issue key") @RequestParam(required = false) String issueKey
    ) throws GitAPIException, IOException {
        gitService.addFiles(pattern, issueKey);
        return "Files added to staging";
    }

    @Operation(summary = "Commit changes to Git")
    @PostMapping("/commit")
    public String commit(
        @Parameter(name = "message", description = "Commit message") @RequestParam String message,
        @Parameter(name = "issueKey", description = "Issue key") @RequestParam String issueKey
    ) throws GitAPIException, IOException {
        gitService.commit(message, issueKey);
        return "Changes committed with message: " + message;
    }

    @Operation(summary = "Push changes to remote repository")
    @PostMapping("/push")
    public String push(
        @Parameter(name = "issueKey", description = "Issue key") @RequestParam String issueKey
    ) throws GitAPIException, IOException {
        gitService.push(issueKey);
        return "Changes pushed to remote";
    }

    @Operation(summary = "Create a new Git branch")
    @PostMapping("/branch")
    public String createBranch(
        @Parameter(name = "branchName", description = "Branch name") @RequestParam String branchName,
        @Parameter(name = "issueKey", description = "Issue key") @RequestParam String issueKey) throws GitAPIException, IOException {
        gitService.createBranch(branchName, issueKey);
        return "Branch created and switched to: " + branchName;
    }

    @Operation(summary = "Reset current branch")
    @PostMapping("/reset")
    public String createBranch(
        @Parameter(name = "issueKey", description = "Issue key") @RequestParam String issueKey
    ) throws GitAPIException, IOException {
        gitService.resetCurrentBranch(issueKey);
        return "Current branch was hard reset";
    }

}
