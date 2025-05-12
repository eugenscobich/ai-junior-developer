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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FilesControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FilesService filesService;

    @InjectMocks
    private FilesController filesController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(filesController).build();
    }

    @Test
    void testListFiles() throws Exception {
        List<String> mockFiles = Arrays.asList("file1.txt", "file2.txt");
        when(filesService.listFiles()).thenReturn(mockFiles);

        this.mockMvc.perform(get("/api/files/listFiles"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[\"file1.txt\", \"file2.txt\"]"));
    }
}