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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

class RunControllerTest {

    @Mock
    private RunService runService;

    @InjectMocks
    private RunController runController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(runController).build();
    }

    @Test
    void run_ShouldReturnRunOutput() throws Exception {
        // Arrange
        String command = "echo Hello World";
        when(runService.run(command)).thenReturn("Hello World\n");

        // Act & Assert
        mockMvc.perform(post("/api/run")
                .param("command", command))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World\n"));
    }
}
