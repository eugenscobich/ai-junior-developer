package ai.junior.developer.service;

import ai.junior.developer.config.ApplicationPropertiesConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;

@Slf4j
@Service
@AllArgsConstructor
public class MavenService {

    private final ApplicationPropertiesConfig applicationPropertiesConfig;
    private static final Logger logger = LoggerFactory.getLogger(MavenService.class);
    private final String MAVEN_PATH = "C:\\apache-maven-3.9.9\\bin\\mvn.cmd";

    public String runCleanInstall(String project) {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", MAVEN_PATH, "clean", "install");
        StringBuilder logs = startMavenCommand(project, processBuilder);

        return logs.toString();
    }

    public String runTests(String project) {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", MAVEN_PATH, "test");
        StringBuilder logs = startMavenCommand(project, processBuilder);

        return logs.toString();
    }

    private StringBuilder startMavenCommand(String project, ProcessBuilder processBuilder) {
        Path workspacePath = applicationPropertiesConfig.getWorkspace().getPath();
        var projectPath = workspacePath.resolve(project);

        StringBuilder logs = new StringBuilder();
        try {
            processBuilder.directory(projectPath.toFile());
            logger.info("Running Maven plugin inside directory: {}", projectPath.toFile().getAbsolutePath());

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(line -> {
                    logger.info("[Output] {}", line);
                    logs.append(line).append("\n");
                });
            }

            int exitCode = process.waitFor();
            logger.info("[Exit Code]: {}", exitCode);
            logs.append("\nExit Code: ").append(exitCode);

        } catch (Exception e) {
            logs.append("Exception occurred: ").append(e.getMessage());
        }
        return logs;
    }
}
