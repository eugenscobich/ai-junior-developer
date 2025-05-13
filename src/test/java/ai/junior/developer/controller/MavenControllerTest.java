package ai.junior.developer.controller;

import ai.junior.developer.service.MavenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class MavenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MavenService mavenService;

    @Test
    @WithMockUser
    public void testRunCleanInstall() throws Exception {
        when(mavenService.runCleanInstall("project")).thenReturn("success");

        mockMvc.perform(get("/api/build/run")
                .param("project", "project"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }
}