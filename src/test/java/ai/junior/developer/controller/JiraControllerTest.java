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
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JiraControllerTest {

    @Mock
    private JiraService jiraService;

    @InjectMocks
    private JiraController jiraController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(jiraController).build();
    }

    @Test
    void webhook_ShouldReturnOkStatus() throws Exception {
        // Arrange
        String requestBody = "{}";
        String xHubSignature = "signature";
        doNothing().when(jiraService).validateRequest(requestBody, xHubSignature);
        doNothing().when(jiraService).webhook(requestBody);

        // Act & Assert
        mockMvc.perform(post("/api/jira/webhook")
                .header("X-Hub-Signature", xHubSignature)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}