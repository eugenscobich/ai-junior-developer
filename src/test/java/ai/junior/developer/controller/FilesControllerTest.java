package ai.junior.developer.controller;

import ai.junior.developer.service.FilesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FilesControllerTest {

    @Mock
    private FilesService filesService;

    @InjectMocks
    private FilesController filesController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListFiles() throws IOException {
        List<String> mockFiles = Arrays.asList("file1.txt", "file2.txt");
        when(filesService.listFiles()).thenReturn(mockFiles);

        List<String> result = filesController.listFiles();

        assertEquals(mockFiles, result);
        verify(filesService).listFiles();
    }

    @Test
    void testWriteFile() throws IOException {
        String path = "test.txt";
        String content = "Hello World";

        filesController.writeFile(path, content);

        verify(filesService).writeFile(path, content);
    }

    @Test
    void testReadFile() throws IOException {
        String path = "test.txt";
        String content = "Hello World";
        when(filesService.readFile(path)).thenReturn(content);

        String result = filesController.readFile(path);

        assertEquals(content, result);
        verify(filesService).readFile(path);
    }

    @Test
    void testReplaceInFile() throws IOException {
        String path = "test.txt";
        String from = "old";
        String to = "new";

        filesController.replaceInFile(path, from, to);

        verify(filesService).replaceInFile(path, from, to);
    }
}