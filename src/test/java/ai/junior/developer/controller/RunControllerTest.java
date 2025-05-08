package ai.junior.developer.controller;

import ai.junior.developer.service.RunService;
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

public class RunControllerTest {

    @Mock
    private RunService runService;

    @InjectMocks
    private RunController runController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(runController).build();
    }

    @Test
    void testRun() throws Exception {
        when(runService.run("command")).thenReturn("Command executed");

        mockMvc.perform(get("/api/run/").param("command", "command"))
                .andExpect(status().isOk())
                .andExpect(content().string("Command executed"));
    }
}