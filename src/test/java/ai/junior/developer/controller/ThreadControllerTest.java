package ai.junior.developer.controller;

import ai.junior.developer.service.ThreadService;
import ai.junior.developer.service.model.MessagesResponse;
import ai.junior.developer.service.model.ThreadsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ThreadController.class)
@AutoConfigureMockMvc(addFilters = false)
class ThreadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ThreadService threadService;

    @Test
    void testGetThreads() throws Exception {
        ThreadsResponse mockResponse = ThreadsResponse.builder().assistantId("assistant-id").threadId("thread-id").build();
        when(threadService.getThreads()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/threads"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testGetMessages() throws Exception {
        MessagesResponse messagesResponse = MessagesResponse.builder().messagesList(List.of()).build();
        when(threadService.getMessages("123")).thenReturn(messagesResponse);

        mockMvc.perform(get("/api/messages/123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
}