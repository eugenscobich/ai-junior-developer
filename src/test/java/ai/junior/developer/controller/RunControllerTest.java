package ai.junior.developer.controller;

import ai.junior.developer.service.RunService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
class RunControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private RunService runService;

    @Autowired
    private RunController runController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(runController).build();
    }

    @Test
    void testRun() throws Exception {
        String command = "echo hello";
        String responseMessage = "hello";

        when(runService.run(command)).thenReturn(responseMessage);

        mockMvc.perform(post("/api/run").param("command", command))
               .andExpect(status().isOk())
               .andExpect(content().string(responseMessage));
    }

    // Additional tests for other endpoints can be written in a similar manner
}