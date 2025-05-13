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
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;

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
    @WithMockUser
    public void testGetThreads() throws Exception {
        ThreadsResponse threadsResponse = ThreadsResponse.builder()
            .assistantId("test-assistant-id")
            .threadId("test-thread-id")
            .build();
        when(threadService.getThreads()).thenReturn(threadsResponse);

        mockMvc.perform(get("/api/threads"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testGetMessages() throws Exception {
        MessagesResponse messagesResponse = MessagesResponse.builder()
            .messagesList(new ArrayList<>())
            .build();
        when(threadService.getMessages("threadId")).thenReturn(messagesResponse);

        mockMvc.perform(get("/api/messages/threadId"))
                .andExpect(status().isOk());
    }
}