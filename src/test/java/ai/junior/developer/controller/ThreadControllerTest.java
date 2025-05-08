package ai.junior.developer.controller;

import ai.junior.developer.service.ThreadService;
import ai.junior.developer.assistant.ThreadTracker;
import ai.junior.developer.service.model.PromptRequest;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ThreadControllerTest {

    @Mock
    private ThreadTracker threadTracker;

    @Mock
    private ThreadService threadService;

    @InjectMocks
    private ThreadController threadController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(threadController).build();
    }

    @Test
    void testGetThreads() throws Exception {
        Map<String, List<String>> threads = new HashMap<>();
        threads.put("assistant1", Collections.singletonList("thread1"));
        when(threadTracker.getAllTracked()).thenReturn(threads);

        mockMvc.perform(get("/api/threads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assistantId").value("assistant1"))
                .andExpect(jsonPath("$.threadId").value("thread1"));
    }

    @Test
    void testSendPromptToExistingThread() throws Exception {
        when(threadService.sendPromptToExistingThread(new PromptRequest())).thenReturn("Response");

        mockMvc.perform(post("/api/prompt/thread")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Response"));
    }
}