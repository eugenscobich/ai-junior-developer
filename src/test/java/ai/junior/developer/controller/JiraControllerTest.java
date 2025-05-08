package ai.junior.developer.controller;

import ai.junior.developer.service.JiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JiraControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JiraService jiraService;

    @InjectMocks
    private JiraController jiraController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(jiraController).build();
    }

    @Test
    void shouldHandleWebhook() throws Exception {
        String requestBody = "{"eventType":"issue_created"}";
        String xHubSignature = "signature";

        doNothing().when(jiraService).validateRequest(requestBody, xHubSignature);

        mockMvc.perform(post("/api/jira/webhook")
                .header("X-Hub-Signature", xHubSignature)
                .content(requestBody)
                .contentType("application/json"))
                .andExpect(status().isOk());
    }
}