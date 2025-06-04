package ai.junior.developer.controller;

import ai.junior.developer.config.ApplicationPropertiesConfig;
import ai.junior.developer.service.GitHubService;
import ai.junior.developer.service.GitHubWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Git Operations", description = "Endpoints to manage Git repository in workspace")
@RestController
@RequestMapping("/api/github")
@AllArgsConstructor
public class GitHubController {

    private final GitHubService githubService;
    private final GitHubWebhookService githubWebhookService;
    private final ApplicationPropertiesConfig config;

    @Operation(summary = "Create a new Git pull request")
    @PostMapping("/pr")
    public String createPullRequest(@RequestParam String title, @RequestParam String description, @RequestParam String threadId)
        throws IOException, InterruptedException {
        githubService.createPullRequest(title, description, threadId);
        return "Pull request created.";
    }

    @Operation(summary = "Get Git pull request number by branch name")
    @GetMapping("/pr/id")
    public Integer getPullRequestNumberByBranchName(
        @RequestParam String owner, @RequestParam String repo,
        @RequestParam String branchName, @RequestParam String apiToken
    ) throws IOException, InterruptedException {
        return githubService.getPullRequestNumberByBranchName(owner, repo, branchName, apiToken);
    }

    @Operation(summary = "Read comments from Git pull request")
    @GetMapping("/pr/comments")
    public List<String> getComments(
        @RequestParam String owner, @RequestParam String repo,
        @RequestParam Integer pullNumber, @RequestParam String apiToken
    ) throws IOException, InterruptedException {
        return githubService.getComments(owner, repo, pullNumber, apiToken);
    }

    @Operation(summary = "GitHub Webhook event")
    @PostMapping("/webhook")
    public void webhook(@RequestBody String payload, @RequestHeader("X-Hub-Signature") String xHubSignature)
        throws Exception {
        githubWebhookService.validateRequest(payload, xHubSignature);
        githubWebhookService.handleWebhook(payload);
    }
}
