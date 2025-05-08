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

class MavenControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MavenService mavenService;

    @InjectMocks
    private MavenController mavenController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(mavenController).build();
    }

    @Test
    void shouldRunCleanInstall() throws Exception {
        String project = "my-project";
        when(mavenService.runCleanInstall(project)).thenReturn("Build success");

        mockMvc.perform(get("/api/build/run")
                .param("project", project))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRunTests() throws Exception {
        String project = "my-project";
        when(mavenService.runTests(project)).thenReturn("Tests success");

        mockMvc.perform(get("/api/build/runtests")
                .param("project", project))
                .andExpect(status().isOk());
    }
}