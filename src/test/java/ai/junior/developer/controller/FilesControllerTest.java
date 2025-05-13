package ai.junior.developer.controller;

import ai.junior.developer.service.FilesService;
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
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class FilesControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private FilesService filesService;

    @Autowired
    private FilesController filesController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(filesController).build();
    }

    @Test
    void testListFiles() throws Exception {
        List<String> files = Arrays.asList("file1.txt", "file2.txt");
        when(filesService.listFiles()).thenReturn(files);

        mockMvc.perform(get("/api/files/listFiles"))
               .andExpect(status().isOk())
               .andExpect(content().string("[\"file1.txt\",\"file2.txt\"]"));
    }

    // Additional tests for other endpoints can be written in a similar manner
}