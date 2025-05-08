package ai.junior.developer.controller;

import ai.junior.developer.assistant.ThreadTracker;
import ai.junior.developer.service.ThreadService;
import ai.junior.developer.service.model.PromptRequest;
import ai.junior.developer.service.model.ThreadsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ThreadControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ThreadTracker threadTracker;
    
    @Mock
    private ThreadService threadService;

    @InjectMocks
    private ThreadController threadController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(threadController).build();
    }

    @Test
    void shouldGetThreads() throws Exception {
        Map<String, List<String>> mockThreads = Map.of("assistant1", List.of("thread1"));
        when(threadTracker.getAllTracked()).thenReturn(mockThreads);

        mockMvc.perform(get("/api/threads"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetMessages() throws Exception {
        String threadId = "thread1";
        Map<String, List<Map<String, Object>>> messages = Collections.emptyMap();
        when(threadService.getMessages(threadId)).thenReturn(messages);

        mockMvc.perform(get("/api/messages/{threadId}", threadId))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSendPromptToExistingThread() throws Exception {
        PromptRequest request = new PromptRequest(); // Populate with required data
        String response = "response";
        when(threadService.sendPromptToExistingThread(request)).thenReturn(response);

        mockMvc.perform(post("/api/prompt/thread")
                .contentType("application/json"))
                .andExpect(status().isOk());
    }
}