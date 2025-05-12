package ai.junior.developer.controller;

import ai.junior.developer.service.MavenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MavenController.class)
@AutoConfigureMockMvc(addFilters = false)
class MavenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MavenService mavenService;

    @Test
    void testRunCleanInstall() throws Exception {
        when(mavenService.runCleanInstall("myProject")).thenReturn("Successful Installation");

        mockMvc.perform(get("/api/build/run")
                .param("project", "myProject"))
                .andExpect(status().isOk());
    }
}