package ai.junior.developer.controller;

import ai.junior.developer.service.FilesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

public class FilesControllerTest {

    @Mock
    private FilesService filesService;

    @InjectMocks
    private FilesController filesController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(filesController).build();
    }

    @Test
    public void testListFiles() throws Exception {
        List<String> filesList = Arrays.asList("file1.txt", "file2.txt");

        when(filesService.listFiles()).thenReturn(filesList);

        mockMvc.perform(get("/api/files/listFiles"))
                .andExpect(status().isOk())
                .andExpect(content().string(filesList.toString()));
    }
    // Additional test methods for other endpoints can be added here
}
