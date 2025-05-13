package ai.junior.developer.controller;

import ai.junior.developer.service.JiraService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
class JiraControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private JiraService jiraService;

    @Autowired
    private JiraController jiraController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jiraController).build();
    }

    @Test
    void testWebhook() throws Exception {
        String requestBody = "{}";
        String xHubSignature = "signature";

        doNothing().when(jiraService).validateRequest(requestBody, xHubSignature);
        doNothing().when(jiraService).webhook(requestBody);

        mockMvc.perform(post("/api/jira/webhook")
                .content(requestBody)
                .header("X-Hub-Signature", xHubSignature))
               .andExpect(status().isOk());
    }

    // Additional tests for other endpoints can be written in a similar manner
}