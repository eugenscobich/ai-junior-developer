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
        this.mockMvc = MockMvcBuilders.standaloneSetup(mavenController).build();
    }

    @Test
    void testRunCleanInstall() throws Exception {
        when(mavenService.runCleanInstall("myProject")).thenReturn("Successful Installation");

        mockMvc.perform(get("/api/build/run")
                .param("project", "myProject"))
                .andExpect(status().isOk());
    }
}