package ai.junior.developer.controller;

import ai.junior.developer.service.GitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GitControllerTest {

    @Mock
    private GitService gitService;

    @InjectMocks
    private GitController gitController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(gitController).build();
    }

    @Test
    void testCloneRepository() throws Exception {
        doNothing().when(gitService).cloneRepository("https://repository.url");

        mockMvc.perform(post("/api/git/clone").param("repoUrl", "https://repository.url"))
                .andExpect(status().isCreated());
    }
}