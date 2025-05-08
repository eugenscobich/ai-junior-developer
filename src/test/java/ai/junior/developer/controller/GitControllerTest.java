package ai.junior.developer.controller;

import ai.junior.developer.service.GitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GitControllerTest {

    @Mock
    private GitService gitService;

    @InjectMocks
    private GitController gitController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(gitController).build();
    }

    @Test
    void cloneRepository_shouldReturnCreated() throws Exception {
        doNothing().when(gitService).cloneRepository("some-repo-url");

        mockMvc.perform(post("/api/git/clone")
            .param("repoUrl", "some-repo-url"))
            .andExpect(status().isCreated());
    }
}