package ai.junior.developer.controller;

import ai.junior.developer.service.FilesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

class FilesControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FilesService filesService;

    @InjectMocks
    private FilesController filesController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(filesController).build();
    }

    @Test
    void shouldListFiles() throws Exception {
        List<String> files = Arrays.asList("file1.txt", "file2.txt");
        when(filesService.listFiles()).thenReturn(files);

        mockMvc.perform(get("/api/files/listFiles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN));
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
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
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