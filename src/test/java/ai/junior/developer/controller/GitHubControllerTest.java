package ai.junior.developer.controller;

import ai.junior.developer.service.GitHubService;
import ai.junior.developer.service.GitHubWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class GitHubControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private GitHubService gitHubService;
    @MockBean
    private GitHubWebhookService gitHubWebhookService;

    @Autowired
    private GitHubController gitHubController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(gitHubController).build();
    }

    @Test
    void testCreatePullRequest() throws Exception {
        String title = "PR Title";
        String description = "PR Description";
        String threadId = "thread123";

        doNothing().when(gitHubService).createPullRequest(title, description, threadId);

        mockMvc.perform(post("/api/github/pr")
                .param("title", title)
                .param("description", description)
                .param("threadId", threadId))
               .andExpect(status().isOk());
    }

    // Additional tests for other endpoints can be written in a similar manner
}