package ai.junior.developer.controller;

import ai.junior.developer.service.FilesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FilesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilesService filesService;

    @Test
    @WithMockUser
    void listFiles_ShouldReturnListOfFiles() throws Exception {
        when(filesService.listFiles()).thenReturn(Arrays.asList("test.txt", "test2.txt"));

        mockMvc.perform(get("/api/files/listFiles"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"test.txt\",\"test2.txt\"]"));
    }

    @Test
    @WithMockUser
    void writeFile_ShouldReturnOkStatus() throws Exception {
        mockMvc.perform(post("/api/files/writeFile")
                        .param("filePath", "test.txt")
                        .param("fileContent", "Hello"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void readFile_ShouldReturnFileContent() throws Exception {
        when(filesService.readFile("test.txt")).thenReturn("Hello World");

        mockMvc.perform(get("/api/files/readFile")
                        .param("filePath", "test.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"));
    }

    @Test
    @WithMockUser
    void deleteFile_ShouldReturnOkStatus() throws Exception {
        mockMvc.perform(get("/api/files/deleteFile")
                        .param("filePath", "test.txt"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void readFiles_ShouldReturnMultipleFilesContent() throws Exception {
        when(filesService.readFiles(Arrays.asList("test1.txt", "test2.txt"))).thenReturn(Arrays.asList("Content1", "Content2"));

        mockMvc.perform(get("/api/files/readFiles")
                        .param("filePaths", "test1.txt")
                        .param("filePaths", "test2.txt"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"Content1\",\"Content2\"]"));
    }

    @Test
    @WithMockUser
    void deleteFiles_ShouldReturnOkStatus() throws Exception {
        mockMvc.perform(get("/api/files/deleteFiles")
                        .param("filePaths", "test1.txt")
                        .param("filePaths", "test2.txt"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void replaceInFile_ShouldReturnAcceptedStatus() throws Exception {
        mockMvc.perform(post("/api/files/replaceInFile")
                        .param("filePath", "test.txt")
                        .param("from", "old")
                        .param("to", "new"))
                .andExpect(status().isOk());
    }
}