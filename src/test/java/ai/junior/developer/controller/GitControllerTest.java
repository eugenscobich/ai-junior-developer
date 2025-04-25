package ai.junior.developer.controller;

import ai.junior.developer.service.GitService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.Mockito.verify;

class GitControllerTest {

    @Mock
    private GitService gitService;

    @InjectMocks
    private GitController gitController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCloneRepository() throws GitAPIException, IOException, URISyntaxException {
        String repoUrl = "https://github.com/example/repo.git";

        gitController.cloneRepository(repoUrl);

        verify(gitService).cloneRepository(repoUrl);
    }

    @Test
    void testAddFiles() throws GitAPIException, IOException {
        String pattern = "*.java";

        gitController.addFiles(pattern);

        verify(gitService).addFiles(pattern);
    }

    @Test
    void testCommit() throws GitAPIException, IOException {
        String message = "Initial commit";

        gitController.commit(message);

        verify(gitService).commit(message);
    }

    @Test
    void testPush() throws GitAPIException, IOException {
        gitController.push();

        verify(gitService).push();
    }

    @Test
    void testCreateBranch() throws GitAPIException, IOException {
        String branchName = "feature/test";

        gitController.createBranch(branchName);

        verify(gitService).createBranch(branchName);
    }
}