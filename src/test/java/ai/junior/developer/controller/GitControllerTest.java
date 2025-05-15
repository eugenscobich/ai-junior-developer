package ai.junior.developer.controller;

import ai.junior.developer.service.GitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.verify;

@WebMvcTest(GitController.class)
class GitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitService gitService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @WithMockUser(username="user", roles={"USER"})
    @Test
    void testCloneRepository() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/git/clone").param("repoUrl", "http://example.com/repo.git"))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        verify(gitService).cloneRepository("http://example.com/repo.git");
    }
}