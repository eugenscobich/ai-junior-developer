package ai.junior.developer.controller;

import ai.junior.developer.service.FilesService;
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

import java.util.Arrays;

import static org.mockito.Mockito.when;

@WebMvcTest(FilesController.class)
class FilesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilesService filesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @WithMockUser(username="user", roles={"USER"})
    @Test
    void testListFiles() throws Exception {
        when(filesService.listFiles()).thenReturn(Arrays.asList("file1.txt", "file2.txt"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/files/listFiles"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("[file1.txt, file2.txt]"));
    }

    @WithMockUser(username="user", roles={"USER"})
    @Test
    void testReadFile() throws Exception {
        when(filesService.readFile("file1.txt")).thenReturn("file content");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/files/readFile").param("filePath", "file1.txt"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("file content"));
    }
}
