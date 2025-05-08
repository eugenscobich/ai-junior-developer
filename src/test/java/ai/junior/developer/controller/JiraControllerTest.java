package ai.junior.developer.controller;

import ai.junior.developer.service.JiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JiraController.class)
class JiraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JiraService jiraService;

    @Test
    void shouldHandleWebhook() throws Exception {
        String requestBody = "{\"eventType\":\"issue_created\"}";
        String xHubSignature = "signature";

        doNothing().when(jiraService).validateRequest(requestBody, xHubSignature);

        mockMvc.perform(post("/api/jira/webhook")
                .header("X-Hub-Signature", xHubSignature)
                .content(requestBody)
                .contentType("application/json"))
                .andExpect(status().isOk());
    }
}