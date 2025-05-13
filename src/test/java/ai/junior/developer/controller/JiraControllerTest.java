package ai.junior.developer.controller;

import ai.junior.developer.service.JiraService;
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
public class JiraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JiraService jiraService;

    @Test
    public void testWebhook() throws Exception {
        doNothing().when(jiraService).validateRequest("requestBody", "xHubSignature");
        doNothing().when(jiraService).webhook("requestBody");

        mockMvc.perform(post("/api/jira/webhook")
                .header("X-Hub-Signature", "xHubSignature")
                .content("requestBody"))
                .andExpect(status().isOk());
    }
}