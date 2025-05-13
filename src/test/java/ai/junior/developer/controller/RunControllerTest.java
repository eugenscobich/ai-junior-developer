package ai.junior.developer.controller;

import ai.junior.developer.service.RunService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class RunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RunService runService;

    @Test
    @WithMockUser
    public void testRun() throws Exception {
        when(runService.run("command")).thenReturn("ran successfully");

        mockMvc.perform(post("/api/run")
                .param("command", "command"))
                .andExpect(status().isOk())
                .andExpect(content().string("ran successfully"));
    }
}