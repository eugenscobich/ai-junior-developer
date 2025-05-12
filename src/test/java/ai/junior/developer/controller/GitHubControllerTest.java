package ai.junior.developer.controller;

import ai.junior.developer.service.GitHubService;
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

class GitHubControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GitHubService githubService;

    @InjectMocks
    private GitHubController githubController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(githubController).build();
    }

    @Test
    void testCreatePullRequest() throws Exception {
        doNothing().when(githubService).createPullRequest("Test PR", "Description");

        mockMvc.perform(post("/api/github/pr")
                .param("title", "Test PR")
                .param("description", "Description"))
                .andExpect(status().isOk());
    }
}