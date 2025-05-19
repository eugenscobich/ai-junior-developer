package ai.junior.developer.service;

import ai.junior.developer.config.ApplicationPropertiesConfig;
import ai.junior.developer.exception.AiJuniorDeveloperException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class FilesService {

    private final ApplicationPropertiesConfig applicationPropertiesConfig;
    private final WorkspaceService workspaceService;

    public List<String> listFiles(String threadId) throws IOException {
        Path workspacePath = workspaceService.getWorkspacePath(threadId);

        List<String> fileList = new ArrayList<>();
        if (Files.exists(workspacePath)) {
            try (var paths = Files.walk(workspacePath)) {
                paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !workspacePath.relativize(path).toString().startsWith(".git"))
                    .filter(path -> !workspacePath.relativize(path).toString().startsWith("traget"))
                    .forEach(path -> fileList.add(workspacePath.relativize(path).toString()));
            }
        }
        log.info("Files are returned: {}", fileList);
        return fileList;
    }

    public String readFile(String filePathStr, String threadId) throws IOException {
        Path workspacePath = workspaceService.getWorkspacePath(threadId);
        var filePath = workspacePath.resolve(filePathStr);

        if (Files.exists(filePath)) {
            if (!Files.isDirectory(filePath)) {
                List<String> lines = Files.readAllLines(filePath);
                var content = String.join("\n", lines);
                log.info("File {} is read", filePathStr);
                return content;
            } else {
                throw new AiJuniorDeveloperException("Requested file path: " + filePathStr + " is a directory. Use listFiles to find the right file path");
            }
        } else {
            throw new AiJuniorDeveloperException("Requested file: " + filePathStr + " does not exist. Use listFiles to find the right file path");
        }
    }

    public void writeFile(String filePathStr, String fileContent, String threadId) throws IOException {
        Path workspacePath = workspaceService.getWorkspacePath(threadId);
        var filePath = workspacePath.resolve(filePathStr);

        if (!Files.exists(filePath)) {
            var parentPath = filePath.getParent();
            if (!Files.exists(parentPath)) {
                log.info("Directories are not existing, create them");
                Files.createDirectories(parentPath);
            }
            log.info("File does not exist, create it");
            Files.createFile(filePath);
        }
        Files.writeString(filePath, fileContent);
        log.info("File {} is written", filePathStr);
    }

    public void replaceInFile(String filePathStr, String from, String to, String threadId) throws IOException {
        Path workspacePath = workspaceService.getWorkspacePath(threadId);
        var filePath = workspacePath.resolve(filePathStr);


        if (Files.exists(filePath)) {
            if (!Files.isDirectory(filePath)) {
                List<String> lines = Files.readAllLines(filePath);
                var content =  String.join("\n", lines);
                var newContent = content.replace(from, to);
                Files.writeString(filePath, newContent);
                if (newContent.equals(content)) {
                    throw new AiJuniorDeveloperException("Replace in file filed. From '" + from + "' does not exist.");
                }
                log.info("File {} is patched\nFrom:\n{}\nTo:\n{}", filePathStr, from, to);
            } else {
                throw new AiJuniorDeveloperException("Requested file path is a directory. Use listFiles to find the right file path");
            }
        } else {
            throw new AiJuniorDeveloperException("Requested file does not exist. Use listFiles to find the right file path");
        }
    }

    public List<String> readFiles(List<String> filePaths, String threadId) throws IOException {
        List<String> filesContent = new ArrayList<>();
        for (String filePath : filePaths) {
            filesContent.add(readFile(filePath, threadId));
        }
        return filesContent;
    }

    public void deleteFile(String filePathStr, String threadId) throws IOException {
        Path workspacePath = workspaceService.getWorkspacePath(threadId);
        var filePath = workspacePath.resolve(filePathStr);
        Files.delete(filePath);
        log.info("File {} is deleted", filePathStr);
    }

    public void deleteFiles(List<String> filePaths, String threadId) throws IOException {
        for (String filePath : filePaths) {
            deleteFile(filePath, threadId);
        }
    }
}
