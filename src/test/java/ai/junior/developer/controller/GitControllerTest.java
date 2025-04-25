package ai.junior.developer.controller;

import ai.junior.developer.service.GitService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(GitController.class)
class GitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitService gitService;

    @Test
    void testCloneRepository() throws Exception {
        String url = "https://github.com/example/repo.git";

        mockMvc.perform(post("/api/git/clone")
                .param("repoUrl", url))
                .andExpect(status().isCreated());

        verify(gitService).cloneRepository(url);
    }

    @Test
    void testAddFiles() throws Exception {
        mockMvc.perform(post("/api/git/add")
                .param("pattern", "*.java"))
                .andExpect(status().isOk())
                .andExpect(content().string("Files added to staging"));

        verify(gitService).addFiles("*.java");
    }

    @Test
    void testCommit() throws Exception {
        mockMvc.perform(post("/api/git/commit")
                .param("message", "Initial commit"))
                .andExpect(status().isOk())
                .andExpect(content().string("Changes committed with message: Initial commit"));

        verify(gitService).commit("Initial commit");
    }

    @Test
    void testPush() throws Exception {
        mockMvc.perform(post("/api/git/push"))
                .andExpect(status().isOk())
                .andExpect(content().string("Changes pushed to remote"));

        verify(gitService).push();
    }

    @Test
    void testCreateBranch() throws Exception {
        mockMvc.perform(post("/api/git/branch")
                .param("branchName", "feature/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Branch created and switched to: feature/test"));

        verify(gitService).createBranch("feature/test");
    }
}