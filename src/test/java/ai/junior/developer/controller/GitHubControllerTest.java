package ai.junior.developer.controller;

import ai.junior.developer.service.GitHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;

import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GitHubControllerTest {

    @Mock
    private GitHubService githubService;

    @InjectMocks
    private GitHubController gitHubController;
    
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(gitHubController).build();
    }

    @Test
    void createPullRequest_shouldReturnOk() throws Exception {
        doNothing().when(githubService).createPullRequest(any(), any());

        mockMvc.perform(post("/api/github/pr")
            .param("title", "title")
            .param("description", "description"))
            .andExpect(status().isOk());
    }
}