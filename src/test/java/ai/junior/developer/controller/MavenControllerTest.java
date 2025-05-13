package ai.junior.developer.controller;

import ai.junior.developer.service.MavenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

class MavenControllerTest {

    @Mock
    private MavenService mavenService;

    @InjectMocks
    private MavenController mavenController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(mavenController).build();
    }

    @Test
    void runCleanInstall_ShouldReturnRunOutput() throws Exception {
        // Arrange
        String project = "ai-junior-developer-backend";
        when(mavenService.runCleanInstall(project)).thenReturn("Build successful");

        // Act & Assert
        mockMvc.perform(get("/api/build/run").param("project", project))
                .andExpect(status().isOk())
                .andExpect(content().string("Build successful"));
    }
}
