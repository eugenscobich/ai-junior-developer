package ai.junior.developer.service;

import ai.junior.developer.config.ApplicationPropertiesConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class WorkspaceService {
    private final ApplicationPropertiesConfig applicationPropertiesConfig;

    public Path getWorkspacePath(String threadId) throws IOException {
        // Clean up if workspacePath already exists
        Path workspacePath = applicationPropertiesConfig.getWorkspace().getPath();
        Path workspace = workspacePath.resolve(threadId);
        if (Files.notExists(workspace)) {
            Files.createDirectories(workspace);
        }
        return workspace;
    }
}
