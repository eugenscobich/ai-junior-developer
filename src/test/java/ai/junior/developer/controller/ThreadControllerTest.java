package ai.junior.developer.controller;

import ai.junior.developer.service.ThreadService;
import ai.junior.developer.service.model.MessagesResponse;
import ai.junior.developer.service.model.ThreadsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(ThreadController.class)
class ThreadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ThreadService threadService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @WithMockUser(username="user", roles={"USER"})
    @Test
    void testGetThreads() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/threads"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}