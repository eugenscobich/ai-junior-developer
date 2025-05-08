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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilesController.class)
class FilesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilesService filesService;

    @Test
    void shouldListFiles() throws Exception {
        List<String> files = List.of("file1.txt", "file2.txt");
        when(filesService.listFiles()).thenReturn(files);

        mockMvc.perform(get("/api/files/listFiles"))
                .andExpect(status().isOk())
                .andExpect(content().string("[file1.txt, file2.txt]"));
    }

    @Test
    void shouldWriteFile() throws Exception {
        String filepath = "file1.txt";
        String content = "Hello World";

        mockMvc.perform(post("/api/files/writeFile")
                .param("filePath", filepath)
                .param("fileContent", content))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReadFile() throws Exception {
        String filepath = "file1.txt";
        String fileContent = "Hello World";
        when(filesService.readFile(filepath)).thenReturn(fileContent);

        mockMvc.perform(get("/api/files/readFile")
                .param("filePath", filepath))
                .andExpect(status().isOk())
                .andExpect(content().string(fileContent));
    }

    @Test
    void shouldDeleteFile() throws Exception {
        String filepath = "file1.txt";

        mockMvc.perform(get("/api/files/deleteFile")
                .param("filePath", filepath))
                .andExpect(status().isOk());
    }
}