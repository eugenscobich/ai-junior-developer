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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GitControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GitService gitService;

    @InjectMocks
    private GitController gitController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(gitController).build();
    }

    @Test
    void testCloneRepository() throws Exception {
        doNothing().when(gitService).cloneRepository("http://example-repo-url");

        mockMvc.perform(post("/api/git/clone")
                .param("repoUrl", "http://example-repo-url"))
                .andExpect(status().isOk());
    }
}