package ai.junior.developer.controller;

import ai.junior.developer.service.GitHubService;
import ai.junior.developer.service.GitHubWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(GitHubController.class)
class GitHubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubService githubService;

    @MockBean
    private GitHubWebhookService githubWebhookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @WithMockUser(username="user", roles={"USER"})
    @Test
    void testCreatePullRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/github/pr")
                .param("title", "New PR")
                .param("description", "Description")
                .param("threadId", "1"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}