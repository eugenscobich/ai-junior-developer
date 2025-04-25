package ai.junior.developer.controller;

import ai.junior.developer.service.FilesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilesController.class)
@WithMockUser(username = "user", password = "password", roles = "USER")
class FilesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilesService filesService;

    @Test
    void testListFiles() throws Exception {
        List<String> files = Arrays.asList("file1.txt", "file2.txt");
        when(filesService.listFiles()).thenReturn(files);

        mockMvc.perform(get("/api/files/listFiles"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"file1.txt\",\"file2.txt\"]"));

        verify(filesService).listFiles();
    }

    @Test
    void testWriteFile() throws Exception {
        mockMvc.perform(post("/api/files/writeFile")
                .param("filePath", "test.txt")
                .param("fileContent", "Hello World")
                .with(csrf()))
                .andExpect(status().isCreated());

        verify(filesService).writeFile("test.txt", "Hello World");
    }

    @Test
    void testReadFile() throws Exception {
        when(filesService.readFile("test.txt")).thenReturn("Hello World");

        mockMvc.perform(get("/api/files/readFile")
                .param("filePath", "test.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"));

        verify(filesService).readFile("test.txt");
    }

    @Test
    void testReplaceInFile() throws Exception {
        mockMvc.perform(post("/api/files/replaceInFile")
                .param("filePath", "test.txt")
                .param("from", "old")
                .param("to", "new")
                .with(csrf()))
                .andExpect(status().isAccepted());

        verify(filesService).replaceInFile("test.txt", "old", "new");
    }
}