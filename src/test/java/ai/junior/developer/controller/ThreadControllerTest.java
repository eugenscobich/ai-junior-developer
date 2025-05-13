package ai.junior.developer.controller;

import ai.junior.developer.service.ThreadService;
import ai.junior.developer.assistant.ThreadTracker;
import ai.junior.developer.service.model.MessagesResponse;
import ai.junior.developer.service.model.PromptRequest;
import ai.junior.developer.service.model.ThreadsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ThreadControllerTest {

    @Mock
    private ThreadService threadService;

    @Mock
    private ThreadTracker threadTracker;

    @InjectMocks
    private ThreadController threadController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(threadController).build();
    }

    @Test
    void getThreads_ShouldReturnTrackedThreads() throws Exception {
        // Arrange
        Map<String, List<String>> trackedThreads = new HashMap<>();
        trackedThreads.put("assistantId", Collections.singletonList("threadId"));
        when(threadTracker.getAllTracked()).thenReturn(trackedThreads);

        // Act & Assert
        mockMvc.perform(get("/api/threads"))
                .andExpect(status().isOk());
    }
}
