package ai.junior.developer.service;

import ai.junior.developer.config.ApplicationPropertiesConfig;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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
}
