package ai.junior.developer.controller;

import ai.junior.developer.service.MavenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MavenController.class)
class MavenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MavenService mavenService;

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