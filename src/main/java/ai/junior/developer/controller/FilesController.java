package ai.junior.developer.controller;

import ai.junior.developer.service.FilesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Git Operations", description = "Endpoints to manage Git repository and files in workspace")
@RestController
@RequestMapping("/api/files")
@AllArgsConstructor
public class FilesController {

    private final FilesService filesService;

    @Operation(summary = "List all files in the workspace")
    @GetMapping("/list")
    public List<String> listFiles(@RequestParam String project) throws IOException {
        return filesService.listFiles(project);
    }
}
