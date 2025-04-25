package ai.junior.developer.controller;

import ai.junior.developer.service.GitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GitController.class)
class GitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitService gitService;

    @Test
    @DisplayName("Clone Repository - Should Return 201")
    void testCloneRepository() throws Exception {
        doNothing().when(gitService).cloneRepository("test-url");
        mockMvc.perform(post("/api/git/clone?repoUrl=test-url"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Add Files - Should Return 200")
    void testAddFiles() throws Exception {
        doNothing().when(gitService).addFiles(null);
        mockMvc.perform(post("/api/git/add"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Commit - Should Return 200")
    void testCommit() throws Exception {
        doNothing().when(gitService).commit("test commit");
        mockMvc.perform(post("/api/git/commit?message=test commit"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Push - Should Return 200")
    void testPush() throws Exception {
        doNothing().when(gitService).push();
        mockMvc.perform(post("/api/git/push"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Create Branch - Should Return 200")
    void testCreateBranch() throws Exception {
        doNothing().when(gitService).createBranch("feature/test");
        mockMvc.perform(post("/api/git/branch?branchName=feature/test"))
                .andExpect(status().isOk());
    }
}