package ai.junior.developer.service;

import ai.junior.developer.config.ApplicationPropertiesConfig;
import ai.junior.developer.service.model.GitHubCreatePullRequestPayload;
import ai.junior.developer.service.model.GitHubCreatePullRequestResponse;
import ai.junior.developer.service.model.GitHubCreateReplyCommentPayload;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@AllArgsConstructor
public class GitHubService {

    private final RestTemplate githubRestTemplate;
    private final WorkspaceService workspaceService;

    public void createPullRequest(String title, String description, String threadId) throws IOException {
        var workspacePath = workspaceService.getWorkspacePath(threadId);
        String prDescription = description + "\n\rThreadId:[" + threadId + "]";
        try (Git git = Git.open(workspacePath.toFile())) {
            // Get the remote URL
            String remoteUrl = git.getRepository().getConfig().getString("remote", "origin", "url");

            Pattern compile = Pattern.compile("git@(.*):(.*)/(.*).git");
            Matcher matcher = compile.matcher(remoteUrl);
            if (matcher.find()) {

                String owner = matcher.group(2);
                String repoName = matcher.group(3);

                // Get the current branch name
                String head = git.getRepository().getBranch();

                GitHubCreatePullRequestPayload gitHubCreatePullRequestPayload = GitHubCreatePullRequestPayload.builder()
                    .title(title)
                    .base("main")
                    .head(head)
                    .body(prDescription)
                    .build();

                String url = "/repos/" + owner + "/" + repoName + "/pulls";
                var response = githubRestTemplate.postForEntity(url, gitHubCreatePullRequestPayload, GitHubCreatePullRequestResponse.class);

                if (response.getStatusCode().value() == 201) {
                    log.info("Pull Request created successfully: {}", response.getBody());
                } else {
                    log.error("Failed to create Pull Request: {} {}", response.getStatusCode(), response.getBody());
                    throw new IOException("Failed to create Pull Request: HTTP " + response.getStatusCode());
                }
            }

        }

    }

    public Integer getPullRequestNumberByBranchName(String owner, String repo, String branchName, String apiToken)
        throws IOException, InterruptedException {
        var url = "https://api.github.com/repos/" + owner + "/" + repo + "/pulls?head=" + owner + ":" + branchName + "&state=all";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + apiToken)
            .header("Accept", "application/vnd.github+json")
            .GET()
            .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            var body = response.body();
            var marker = "\"number\":";
            int idx = body.indexOf(marker);
            if (idx != -1) {
                int start = idx + marker.length();
                int end = body.indexOf(",", start);
                String numberStr = body.substring(start, end).trim();
                return Integer.parseInt(numberStr);
            } else {
                throw new IOException("No pull request found for branch: " + branchName);
            }
        } else {
            throw new IOException("Failed to find pull request: HTTP " + response.statusCode());
        }
    }

    public List<String> getComments(String owner, String repo, int pullNumber, String apiToken) throws IOException, InterruptedException {
        var apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/pulls/" + pullNumber + "/comments";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Authorization", "Bearer " + apiToken)
            .header("Accept", "application/vnd.github+json")
            .GET()
            .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            log.info("Extracted comments from pull request: {} ", pullNumber);
            return extractComments(response.body());
        } else {
            log.error("Failed to fetch pull request comments: {} {}", response.statusCode(), response.body());
            throw new IOException("Failed to fetch PR comments: HTTP " + response.statusCode());
        }
    }

    private List<String> extractComments(String json) {
        List<String> comments = new ArrayList<>();

        int index = 0;
        while ((index = json.indexOf("\"body\":\"", index)) != -1) {
            index += "\"body\":\"".length();
            int end = json.indexOf("\"", index);
            if (end > index) {
                String comment = json.substring(index, end)
                    .replace("\\n", "\n")    // unescape newlines
                    .replace("\\\"", "\"");  // unescape quotes
                comments.add(comment);
            }
        }

        return comments;
    }

    public void addComment(String prUrl, String commentId, String result, boolean isReplyComment) {
        var url = prUrl + "/comments" + (isReplyComment ? "/" + commentId + "/replies" : "");
        GitHubCreateReplyCommentPayload gitHubCreateReplyCommentPayload = GitHubCreateReplyCommentPayload.builder()
            .body(result)
            .build();
        var response = githubRestTemplate.postForEntity(url, gitHubCreateReplyCommentPayload, String.class);
        log.info("Added reply comment to {}. Response: {}", commentId, response);
    }

}