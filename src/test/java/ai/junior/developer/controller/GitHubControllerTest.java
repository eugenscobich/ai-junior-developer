package ai.junior.developer.controller;

import ai.junior.developer.service.GitHubService;
import ai.junior.developer.service.GitHubWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GitHubControllerTest {

    @Mock
    private GitHubService githubService;

    @Mock
    private GitHubWebhookService githubWebhookService;

    @InjectMocks
    private GitHubController githubController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(githubController).build();
    }

    @Test
    void createPullRequest_ShouldReturnCreatedStatus() throws Exception {
        // Arrange
        String prTitle = "PR Title";
        String prDescription = "PR Description";
        String threadId = "1";
        doNothing().when(githubService).createPullRequest(prTitle, prDescription, threadId);

        // Act & Assert
        mockMvc.perform(post("/api/github/pr")
                .param("title", prTitle)
                .param("description", prDescription)
                .param("threadId", threadId))
                .andExpect(status().isOk());
    }
}
