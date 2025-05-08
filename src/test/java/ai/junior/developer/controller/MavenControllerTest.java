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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class MavenControllerTest {

    @Mock
    private MavenService mavenService;

    @InjectMocks
    private MavenController mavenController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(mavenController).build();
    }

    @Test
    void testRunCleanInstall() throws Exception {
        when(mavenService.runCleanInstall("project")).thenReturn("Build successful");

        mockMvc.perform(get("/api/build/run").param("project", "project"))
                .andExpect(status().isOk())
                .andExpect(content().string("Build successful"));
    }
}