package ai.junior.developer.controller;

import ai.junior.developer.service.ThreadService;
import ai.junior.developer.service.model.MessagesResponse;
import ai.junior.developer.service.model.ThreadsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ThreadControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ThreadService threadService;

    @InjectMocks
    private ThreadController threadController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(threadController).build();
    }

    @Test
    void testGetThreads() throws Exception {
        ThreadsResponse mockResponse = ThreadsResponse.builder().assistantId("assistant-id").threadId("thread-id").build();
        when(threadService.getThreads()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/threads"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetMessages() throws Exception {
        MessagesResponse messagesResponse = MessagesResponse.builder().messagesList(List.of()).build();
        when(threadService.getMessages("123")).thenReturn(messagesResponse);

        mockMvc.perform(get("/api/messages/123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}