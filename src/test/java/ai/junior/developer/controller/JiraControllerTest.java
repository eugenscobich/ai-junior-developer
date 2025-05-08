package ai.junior.developer.controller;

import ai.junior.developer.service.JiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class JiraControllerTest {

    @Mock
    private JiraService jiraService;

    @InjectMocks
    private JiraController jiraController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(jiraController).build();
    }

    @Test
    void testWebhook() throws Exception {
        doNothing().when(jiraService).validateRequest("data", "signature");
        doNothing().when(jiraService).webhook("data");

        mockMvc.perform(post("/api/jira/webhook")
                .header("X-Hub-Signature", "signature")
                .content("data")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
