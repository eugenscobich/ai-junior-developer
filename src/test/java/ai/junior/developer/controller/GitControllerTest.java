package ai.junior.developer.controller;

import ai.junior.developer.service.GitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class GitControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private GitService gitService;

    @Autowired
    private GitController gitController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(gitController).build();
    }

    @Test
    void testCloneRepository() throws Exception {
        String repoUrl = "http://example.com/repo.git";
        doNothing().when(gitService).cloneRepository(repoUrl);

        mockMvc.perform(post("/api/git/clone").param("repoUrl", repoUrl))
               .andExpect(status().isOk());
    }

    // Additional tests for other endpoints can be written in a similar manner
}