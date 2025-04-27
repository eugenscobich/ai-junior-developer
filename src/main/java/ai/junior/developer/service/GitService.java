package ai.junior.developer.service;

import ai.junior.developer.config.ApplicationPropertiesConfig;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

@Slf4j
@Service
@AllArgsConstructor
public class GitService {

    private final ApplicationPropertiesConfig applicationPropertiesConfig;

    public void cloneRepository(String repoUrl) throws IOException, GitAPIException, URISyntaxException {
        Path workspacePath = applicationPropertiesConfig.getWorkspace().getPath();
        if (Files.exists(workspacePath)) {
            FileSystemUtils.deleteRecursively(workspacePath);
        }
        Files.createDirectories(workspacePath);
        // Clone
        try (var git = Git.cloneRepository()
            .setURI(repoUrl)
            .setDirectory(workspacePath.toFile())
            .setTransportConfigCallback(transport -> {
                if (transport instanceof SshTransport sshTransport) {
                    sshTransport.setSshSessionFactory(new SshdSessionFactory());
                }
            })
            .call()) {
            git.remoteSetUrl()
                .setRemoteName("origin")
                .setRemoteUri(new URIish(repoUrl))
                .call();
            log.info("Cloned repository and updated the remote url");
        }

    }

    private Path resolveRepoPath(Path workspacePath, String repoUrl) throws IOException {
        var projectPattern = Pattern.compile("/(.*)\\.git$");
        var matcher = projectPattern.matcher(repoUrl);
        if (matcher.find()) {
            var projectName = matcher.group(1);
            log.info("Using project name {}", projectName);
            var projectPath = workspacePath.resolve(projectName);
            if (Files.notExists(projectPath)) {
                Files.createDirectories(projectPath);
            }
            return projectPath;
        } else {
            return workspacePath;
        }
    }

    private Path getWorkspacePath() throws IOException {
        // Clean up if workspacePath already exists
        Path workspacePath = applicationPropertiesConfig.getWorkspace().getPath();
        if (Files.notExists(workspacePath)) {
            Files.createDirectories(workspacePath);
        }
        return workspacePath;
    }

    public void addFiles(String pattern) throws IOException, GitAPIException {
        var workspacePath = getWorkspacePath();
        try (Git git = Git.open(workspacePath.toFile())) {
            git.add().addFilepattern(pattern == null ? "." : pattern).call();
            log.info("Files added to staging");
        }
    }

    public void commit(String message) throws IOException, GitAPIException {
        var workspacePath = getWorkspacePath();
        try (Git git = Git.open(workspacePath.toFile())) {
            git.commit().setMessage(message).call();
            log.info("Committed with message: {}", message);
        }
    }

    public void push() throws IOException, GitAPIException {
        var workspacePath = getWorkspacePath();
        try (Git git = Git.open(workspacePath.toFile())) {
            git.push()
                .setTransportConfigCallback(transport -> {
                    if (transport instanceof SshTransport sshTransport) {
                        sshTransport.setSshSessionFactory(new SshdSessionFactory());
                    }
                })
                .call();
            log.info("Pushed changes");
        }
    }

    public void createBranch(String branchName) throws IOException, GitAPIException {
        var workspacePath = getWorkspacePath();
        try (Git git = Git.open(workspacePath.toFile())) {
            git.checkout()
                .setCreateBranch(true)
                .setName(branchName)
                .call();
            log.info("Branch {} created", branchName);
        }
    }

    public void createPullRequest(String owner, String repo, String branchName, String apiToken) throws IOException,
            InterruptedException {
        var apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/pulls";

        var title = "Automated Pull Request for branch " + branchName;
        var body = "This pull request was created automatically by AI.";

        var json = """
                {
                  "title": "%s",
                  "head": "%s",
                  "base": "main",
                  "body": "%s"
                }
                """.formatted(title, branchName, body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + apiToken)
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

    public Integer getPullRequestNumberByBranch(String owner, String repo, String branchName, String apiToken) throws IOException, InterruptedException {
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
