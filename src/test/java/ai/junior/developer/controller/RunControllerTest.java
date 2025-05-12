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

class RunControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RunService runService;

    @InjectMocks
    private RunController runController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(runController).build();
    }

    @Test
    void testRun() throws Exception {
        when(runService.run("someCommand")).thenReturn("Execution Result");

        mockMvc.perform(post("/api/run")
                .param("command", "someCommand"))
                .andExpect(status().isOk());
    }
}