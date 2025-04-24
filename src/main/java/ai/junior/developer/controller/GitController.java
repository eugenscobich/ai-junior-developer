package ai.junior.developer.controller;

import ai.junior.developer.controller.model.ErrorResponse;
import ai.junior.developer.service.GitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Git Operations", description = "Endpoints to manage Git repository and files in workspace")
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
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server error", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            })
        })
    @PostMapping("/clone")
    public void cloneRepository(@RequestParam("repoUrl") String repoUrl) throws GitAPIException, IOException {
        gitService.cloneRepository(repoUrl);
    }

    @Operation(summary = "Add files to Git staging area")
    @PostMapping("/add")
    public String addFiles(@RequestParam String project, @RequestParam(required = false) String pattern) throws GitAPIException, IOException {
        gitService.addFiles(project, pattern);
        return "Files added to staging";
    }

    @Operation(summary = "Commit changes to Git")
    @PostMapping("/commit")
    public String commit(@RequestParam String project, @RequestParam String message) throws GitAPIException, IOException {
        gitService.commit(project, message);
        return "Changes committed with message: " + message;
    }


    @Operation(summary = "Push changes to remote repository")
    @PostMapping("/push")
    public String push(@RequestParam String project) throws GitAPIException, IOException {
        gitService.push(project);
        return "Changes pushed to remote";
    }


    @Operation(summary = "Create a new Git branch")
    @PostMapping("/branch")
    public String createBranch(@RequestParam String project, @RequestParam String branchName) throws GitAPIException, IOException {
        gitService.createBranch(project, branchName);
        return "Branch created and switched to: " + branchName;
    }
}
