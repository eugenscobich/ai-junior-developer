package ai.junior.developer.controller;

import ai.junior.developer.service.JiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(JiraController.class)
class JiraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JiraService jiraService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @WithMockUser(username="user", roles={"USER"})
    @Test
    void testWebhook() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/jira/webhook")
                .header("X-Hub-Signature", "signature")
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}