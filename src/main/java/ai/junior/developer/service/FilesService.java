package ai.junior.developer.service;

import ai.junior.developer.config.ApplicationPropertiesConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class FilesService {

    private final ApplicationPropertiesConfig applicationPropertiesConfig;

    public List<String> listFiles(String project) throws IOException {
        Path workspacePath = applicationPropertiesConfig.getWorkspace().getPath();
        var projectPath = workspacePath.resolve(project);

        List<String> fileList = new ArrayList<>();
        if (Files.exists(projectPath)) {
            try (var paths = Files.walk(projectPath)) {
                paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !projectPath.relativize(path).toString().startsWith(".git"))
                    .filter(path -> !projectPath.relativize(path).toString().startsWith("traget"))
                    .forEach(path -> fileList.add(projectPath.relativize(path).toString()));
            }
        }
        return fileList;
    }
}
