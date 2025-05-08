package ai.junior.developer.controller;

import ai.junior.developer.service.GitHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GitHubController.class)
class GitHubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubService githubService;

    @Test
    void shouldCreatePullRequest() throws Exception {
        String title = "New Feature";
        String description = "Implementing a new feature";

        mockMvc.perform(post("/api/github/pr")
                .param("title", title)
                .param("description", description))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetPullRequestNumberByBranchName() throws Exception {
        String owner = "user";
        String repo = "repo";
        String branchName = "feature-branch";
        String apiToken = "token";
        Integer prNumber = 1;

        when(githubService.getPullRequestNumberByBranchName(owner, repo, branchName, apiToken)).thenReturn(prNumber);

        mockMvc.perform(get("/api/github/pr/id")
                .param("owner", owner)
                .param("repo", repo)
                .param("branchName", branchName)
                .param("apiToken", apiToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetComments() throws Exception {
        String owner = "user";
        String repo = "repo";
        Integer pullNumber = 1;
        String apiToken = "token";

        mockMvc.perform(get("/api/github/pr/comments")
                .param("owner", owner)
                .param("repo", repo)
                .param("pullNumber", String.valueOf(pullNumber))
                .param("apiToken", apiToken))
                .andExpect(status().isOk());
    }
}