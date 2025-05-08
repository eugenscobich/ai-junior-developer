package ai.junior.developer.controller;

import ai.junior.developer.service.RunService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RunControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RunService runService;

    @InjectMocks
    private RunController runController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(runController).build();
    }

    @Test
    void shouldPerformLocalRun() throws Exception {
        String command = "echo 'Hello World'";
        when(runService.run(command)).thenReturn("Hello World");

        mockMvc.perform(get("/api/run/")
                .param("command", command))
                .andExpect(status().isOk());
    }
}