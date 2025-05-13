package ai.junior.developer.controller;

import ai.junior.developer.service.GitHubService;
import ai.junior.developer.service.GitHubWebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GitHubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubService gitHubService;

    @MockBean
    private GitHubWebhookService gitHubWebhookService;

    @Test
    public void testCreatePullRequest() throws Exception {
        doNothing().when(gitHubService).createPullRequest("title", "description", "threadId");

        mockMvc.perform(post("/api/github/pr")
                .param("title", "title")
                .param("description", "description")
                .param("threadId", "threadId"))
                .andExpect(status().isOk());
    }
}