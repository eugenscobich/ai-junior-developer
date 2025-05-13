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

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FilesControllerTest {

    @Mock
    private FilesService filesService;

    @InjectMocks
    private FilesController filesController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(filesController).build();
    }

    @Test
    void listFiles_ShouldReturnListOfFiles() throws Exception {
        when(filesService.listFiles()).thenReturn(Arrays.asList("test.txt", "test2.txt"));

        mockMvc.perform(get("/api/files/listFiles"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"test.txt\",\"test2.txt\"]"));
    }

    @Test
    void writeFile_ShouldReturnCreatedStatus() throws Exception {
        mockMvc.perform(post("/api/files/writeFile")
                        .param("filePath", "test.txt")
                        .param("fileContent", "Hello"))
                .andExpect(status().isOk());
    }

    @Test
    void readFile_ShouldReturnFileContent() throws Exception {
        when(filesService.readFile("test.txt")).thenReturn("Hello World");

        mockMvc.perform(get("/api/files/readFile")
                        .param("filePath", "test.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"));
    }

    @Test
    void deleteFile_ShouldReturnOkStatus() throws Exception {
        mockMvc.perform(get("/api/files/deleteFile")
                        .param("filePath", "test.txt"))
                .andExpect(status().isOk());
    }

    @Test
    void readFiles_ShouldReturnMultipleFilesContent() throws Exception {
        when(filesService.readFiles(Arrays.asList("test1.txt", "test2.txt"))).thenReturn(Arrays.asList("Content1", "Content2"));

        mockMvc.perform(get("/api/files/readFiles")
                        .param("filePaths", "test1.txt")
                        .param("filePaths", "test2.txt"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"Content1\",\"Content2\"]"));
    }

    @Test
    void deleteFiles_ShouldReturnOkStatus() throws Exception {
        mockMvc.perform(get("/api/files/deleteFiles")
                        .param("filePaths", "test1.txt")
                        .param("filePaths", "test2.txt"))
                .andExpect(status().isOk());
    }

    @Test
    void replaceInFile_ShouldReturnAcceptedStatus() throws Exception {
        mockMvc.perform(post("/api/files/replaceInFile")
                        .param("filePath", "test.txt")
                        .param("from", "old")
                        .param("to", "new"))
                .andExpect(status().isOk());
    }
}