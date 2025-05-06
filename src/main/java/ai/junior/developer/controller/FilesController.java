package ai.junior.developer.controller;

import ai.junior.developer.service.FilesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Files Operations", description = "Endpoints to manage files in workspace")
@RestController
@RequestMapping("/api/files")
@AllArgsConstructor
public class FilesController {

    private final FilesService filesService;

    @Operation(
        operationId = "listFiles",
        summary = "List all files in workspace",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK, List of files in workspace", content = {
                @Content(mediaType = "text/plain", schema = @Schema(implementation = List.class), examples = @ExampleObject(summary = "[src\\\\main\\\\resources\\\\static\\\\index.html,\"src\\\\main\\\\resources\\\\application.yml\"]"))
            }),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
        }
    )
    @GetMapping("/listFiles")
    public List<String> listFiles() throws IOException {
        return filesService.listFiles();
    }

    @Operation(
        operationId = "writeFile",
        summary = "Overwrite a file with the complete content given in one step. You cannot append to a file or write parts or write parts - use replaceInFile for inserting parts",
        responses = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
        })
    @PostMapping("/writeFile")
    public void writeFile(
        @Parameter(name = "filePath", description = "File path to create and write/override the content") @RequestParam("filePath") String filePath,
        @Parameter(name = "fileContent", description = "File content to save") @RequestParam("fileContent") String fileContent
    ) throws IOException {
        filesService.writeFile(filePath, fileContent);
    }

    @Operation(
        operationId = "readFile",
        summary = "Read file content",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK. Content of the requested file"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
        })
    @GetMapping("/readFile")
    public String readFile(
        @Parameter(name = "filePath", description = "File path to read") @RequestParam("filePath") String filePath
    ) throws IOException {
        return filesService.readFile(filePath);
    }

    @Operation(
        operationId = "deleteFile",
        summary = "Delete file",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
        })
    @GetMapping("/deleteFile")
    public void deleteFile(
        @Parameter(name = "filePath", description = "File path to delete") @RequestParam("filePath") String filePath
    ) throws IOException {
        filesService.deleteFile(filePath);
    }

    @Operation(
        operationId = "readFiles",
        summary = "Read multiple file content",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK, List of files content", content = {
                @Content(mediaType = "text/plain", schema = @Schema(implementation = List.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
        })
    @GetMapping("/readFiles")
    public List<String> readFiles(
        @Parameter(name = "filePaths", description = "File paths to read") @RequestParam("filePaths") List<String> filePaths
    ) throws IOException {
        return filesService.readFiles(filePaths);
    }

    @Operation(
        operationId = "deleteFiles",
        summary = "Delete files",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
        })
    @GetMapping("/deleteFiles")
    public void deleteFiles(
        @Parameter(name = "filePaths", description = "File paths to delete") @RequestParam("filePaths") List<String> filePaths
    ) throws IOException {
        filesService.deleteFiles(filePaths);
    }

    @Operation(
        operationId = "replaceInFile",
        summary = "replaceInFile",
        responses = {
            @ApiResponse(responseCode = "202", description = "Accepted. replaceInFile"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
        })
    @PostMapping("/replaceInFile")
    public void replaceInFile(
        @Parameter(name = "filePath", description = "File path to read") @RequestParam("filePath") String filePath,
        @Parameter(name = "from", description = "String that need to be replaced")
        @RequestParam("from") String from,
        @Parameter(name = "to", description = "String that will be replaced with")
        @RequestParam("to") String to
    ) throws IOException {
        filesService.replaceInFile(filePath, from, to);
    }
}
