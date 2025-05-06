package ai.junior.developer.service;

import ai.junior.developer.config.ApplicationPropertiesConfig;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class GitHubService {

    private final ApplicationPropertiesConfig applicationPropertiesConfig;

    private Path getWorkspacePath() throws IOException {
        // Clean up if workspacePath already exists
        Path workspacePath = applicationPropertiesConfig.getWorkspace().getPath();
        if (Files.notExists(workspacePath)) {
            Files.createDirectories(workspacePath);
        }
        return workspacePath;
    }

    public void createPullRequest(String title, String description) throws IOException,
        InterruptedException {
        var workspacePath = getWorkspacePath();
        try (Git git = Git.open(workspacePath.toFile())) {
            // Get the remote URL
            String remoteUrl = git.getRepository().getConfig().getString("remote", "origin", "url");

            // Extract owner and repo from URL
            String[] urlParts = remoteUrl.split("/");
            String owner = urlParts[urlParts.length - 2];
            String repo = urlParts[urlParts.length - 1].replace(".git", "");

            // Get the current branch name
            String head = git.getRepository().getBranch();

            // Construct the PR creation API call
            var apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/pulls";

            var json = String.format(
                """
                    {
                      "title": "%s",
                      "head": "%s",
                      "base": "main",
                      "body": "%s"
                    }
                    """, title, head, description
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + applicationPropertiesConfig.getGithub().getApiToken())
                .header("Accept", "application/vnd.github+json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                log.info("Pull Request created successfully: {}", response.body());
            } else {
                log.error("Failed to create Pull Request: {} {}", response.statusCode(), response.body());
                throw new IOException("Failed to create Pull Request: HTTP " + response.statusCode());
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
}