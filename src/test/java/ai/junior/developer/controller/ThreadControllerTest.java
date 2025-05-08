package ai.junior.developer.controller;

import ai.junior.developer.assistant.ThreadTracker;
import ai.junior.developer.service.ThreadService;
import ai.junior.developer.service.model.ThreadsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ThreadControllerTest {

    @Mock
    private ThreadTracker threadTracker;

    @Mock
    private ThreadService threadService;

    @InjectMocks
    private ThreadController threadController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(threadController).build();
    }

    @Test
    void getThreads_shouldReturnOk() throws Exception {
        Map<String, List<String>> activeThreads = Collections.singletonMap("assistantId", List.of("threadId"));
        when(threadTracker.getAllTracked()).thenReturn(activeThreads);

        mockMvc.perform(get("/api/threads"))
            .andExpect(status().isOk());
    }

    @Test
    void sendPromptToExistingThread_shouldReturnOk() throws Exception {
        when(threadService.sendPromptToExistingThread(any()))
            .thenReturn("Response");

        mockMvc.perform(post("/api/prompt/thread")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk());
    }
}