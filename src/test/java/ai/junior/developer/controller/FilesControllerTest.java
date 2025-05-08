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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void listFiles_shouldReturnOk() throws Exception {
        List<String> mockList = Arrays.asList("file1.txt", "file2.txt");
        when(filesService.listFiles()).thenReturn(mockList);

        mockMvc.perform(get("/api/files/listFiles"))
            .andExpect(status().isOk());
    }

    @Test
    void writeFile_shouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/files/writeFile")
            .param("filePath", "file1.txt")
            .param("fileContent", "This is test content"))
            .andExpect(status().isCreated());
    }

    @Test
    void readFile_shouldReturnOk() throws Exception {
        when(filesService.readFile(any())).thenReturn("This is test content");

        mockMvc.perform(get("/api/files/readFile")
            .param("filePath", "file1.txt"))
            .andExpect(status().isOk());
    }
}