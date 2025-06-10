package ai.junior.developer.service;

import ai.junior.developer.config.ApplicationPropertiesConfig;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class GitService {

    private final ApplicationPropertiesConfig applicationPropertiesConfig;
    private final WorkspaceService workspaceService;

    public void cloneRepository(String repoUrl, String threadId) throws IOException, GitAPIException, URISyntaxException {
        Path workspacePath = workspaceService.getWorkspacePath(threadId);

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

    public void addFiles(String pattern, String threadId) throws IOException, GitAPIException {
        var workspacePath = workspaceService.getWorkspacePath(threadId);
        try (Git git = Git.open(workspacePath.toFile())) {
            git.add().addFilepattern(pattern == null ? "." : pattern).call();
            log.info("Add to git staging: {}", pattern);
        }
    }

    public void commit(String message, String threadId) throws IOException, GitAPIException {
        var workspacePath = workspaceService.getWorkspacePath(threadId);
        try (Git git = Git.open(workspacePath.toFile())) {
            git.commit().setMessage(message).call();
            log.info("Git commit with message: {}", message);
        }
    }

    public void push(String threadId) throws IOException, GitAPIException {
        var workspacePath = workspaceService.getWorkspacePath(threadId);
        try (Git git = Git.open(workspacePath.toFile())) {
            String branch = git.getRepository().getBranch();
            Iterable<PushResult> origin = git.push()
                .setRemote("origin")
                .setRefSpecs(new RefSpec("refs/heads/" + branch))
                .setForce(true)
                .setTransportConfigCallback(transport -> {
                    if (transport instanceof SshTransport sshTransport) {
                        sshTransport.setSshSessionFactory(new SshdSessionFactory());
                    }
                })
                .call();
            log.info("Git pushed to: {}",  branch);
        }
    }

    public void createBranch(String branchName, String threadId) throws IOException, GitAPIException {
        var normalizedBranchName = branchName.replaceAll("[^a-zA-Z0-9-_]", "-");
        var workspacePath = workspaceService.getWorkspacePath(threadId);
        try (Git git = Git.open(workspacePath.toFile())) {
            git.checkout()
                .setCreateBranch(true)
                .setName(normalizedBranchName)
                .call();
            log.info("Create branch: {}", branchName);
        }
    }

    public void resetCurrentBranch(String threadId) throws IOException, GitAPIException {
        var workspacePath = workspaceService.getWorkspacePath(threadId);
        try (Git git = Git.open(workspacePath.toFile())) {
            git.reset()
                .setMode(ResetType.HARD)
                .call();
            log.info("Current branch was hard reset");
        }
    }

    public void resetAFile(String filePath, String threadId) throws IOException, GitAPIException {
        var workspacePath = workspaceService.getWorkspacePath(threadId);
        try (Git git = Git.open(workspacePath.toFile())) {
            git.reset()
                .addPath(filePath)
                .setMode(ResetType.HARD)
                .call();
            log.info("Reset {} file", filePath);
        }
    }

    public void deleteAFile(String filePath, String threadId) throws IOException, GitAPIException {
        var workspacePath = workspaceService.getWorkspacePath(threadId);
        try (Git git = Git.open(workspacePath.toFile())) {
            git.rm()
                .addFilepattern(filePath)
                .call();
            log.info("Git delete a file: {}", filePath);
        }
    }
}
