package ai.junior.developer.controller;

import ai.junior.developer.service.JiraService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    void webhook_shouldReturnOk() throws Exception {
        doNothing().when(jiraService).validateRequest("{}", "signature");
        doNothing().when(jiraService).webhook("{}");

        mockMvc.perform(post("/api/jira/webhook")
            .header("X-Hub-Signature", "signature")
            .content("{}"))
            .andExpect(status().isOk());
    }
}