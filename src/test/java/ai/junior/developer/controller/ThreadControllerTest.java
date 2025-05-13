package ai.junior.developer.controller;

import ai.junior.developer.service.ThreadService;
import ai.junior.developer.service.model.MessagesResponse;
import ai.junior.developer.service.model.ThreadsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ThreadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ThreadService threadService;

    @Test
    public void testGetThreads() throws Exception {
        ThreadsResponse threadsResponse = new ThreadsResponse();
        when(threadService.getThreads()).thenReturn(threadsResponse);

        mockMvc.perform(get("/api/threads"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetMessages() throws Exception {
        MessagesResponse messagesResponse = new MessagesResponse();
        when(threadService.getMessages("threadId")).thenReturn(messagesResponse);

        mockMvc.perform(get("/api/messages/threadId"))
                .andExpect(status().isOk());
    }
}