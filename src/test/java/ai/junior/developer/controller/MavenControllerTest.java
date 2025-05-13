package ai.junior.developer.controller;

import ai.junior.developer.service.MavenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
class MavenControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private MavenService mavenService;

    @Autowired
    private MavenController mavenController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mavenController).build();
    }

    @Test
    void testRunCleanInstall() throws Exception {
        String project = "myProject";
        String responseMessage = "Clean install executed";

        when(mavenService.runCleanInstall(project)).thenReturn(responseMessage);

        mockMvc.perform(get("/api/build/run").param("project", project))
               .andExpect(status().isOk())
              .andExpect(content().string(responseMessage));
    }

    // Additional tests for other endpoints can be written in a similar manner
}