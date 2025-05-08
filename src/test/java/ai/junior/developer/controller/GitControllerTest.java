package ai.junior.developer.controller;

import ai.junior.developer.service.GitService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GitController.class)
class GitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitService gitService;

    @Test
    void shouldCloneRepository() throws Exception {
        String repoUrl = "git@github.com:eugenpublic/ai-junior-developer.git";
        
        mockMvc.perform(post("/api/git/clone")
                .param("repoUrl", repoUrl))
                .andExpect(status().isCreated());
    }
    
    @Test
    void shouldAddFiles() throws Exception {
        
        mockMvc.perform(post("/api/git/add"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCommitChanges() throws Exception {
        String message = "Initial commit";

        mockMvc.perform(post("/api/git/commit")
                .param("message", message))
                .andExpect(status().isOk());
    }

    @Test
    void shouldPushChanges() throws Exception {

        mockMvc.perform(post("/api/git/push"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateBranch() throws Exception {
        String branchName = "feature-branch";

        mockMvc.perform(post("/api/git/branch")
                .param("branchName", branchName))
                .andExpect(status().isOk());
    }
}