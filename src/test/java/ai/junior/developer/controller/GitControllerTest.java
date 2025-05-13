package ai.junior.developer.controller;

import ai.junior.developer.service.GitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
    void cloneRepository_ShouldReturnCreatedStatus() throws Exception {
        // Arrange
        String repoUrl = "https://github.com/test/repo.git";
        doNothing().when(gitService).cloneRepository(repoUrl);

        // Act & Assert
        mockMvc.perform(post("/api/git/clone")
                .param("repoUrl", repoUrl))
                .andExpect(status().isOk());
    }
}
