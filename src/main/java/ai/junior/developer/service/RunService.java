package ai.junior.developer.service;

import ai.junior.developer.config.ApplicationPropertiesConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class RunService {

    private final ApplicationPropertiesConfig applicationPropertiesConfig;

    public String run(String command) throws IOException, InterruptedException {
        var args = Arrays.asList(command.split(" "));
        args.addFirst("/c");
        args.addFirst("powershell.exe");
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        StringBuilder logs = runCommand(command, processBuilder);

        return logs.toString();
    }

    private StringBuilder runCommand(String command, ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Path workspacePath = applicationPropertiesConfig.getWorkspace().getPath();

        StringBuilder logs = new StringBuilder();

        processBuilder.directory(workspacePath.toFile());
        log.info("Running {} inside directory: {}", command, workspacePath.toFile().getAbsolutePath());

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(line -> {
                log.info("{}", line);
                logs.append(line).append("\n");
            });
        }

        int exitCode = process.waitFor();
        log.info("[Exit Code]: {}", exitCode);
        logs.append("\nExit Code: ").append(exitCode);
        return logs;
    }
}
