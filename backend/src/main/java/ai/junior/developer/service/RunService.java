package ai.junior.developer.service;

import ai.junior.developer.config.ApplicationPropertiesConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class RunService {

    private final ApplicationPropertiesConfig applicationPropertiesConfig;
    private final WorkspaceService workspaceService;

    public String run(String command, String issueKey) throws IOException, InterruptedException {
        var args = new ArrayList<String>(Arrays.asList(command.split(" ")));
        if (isWindows()) {
            args.addFirst("/c");
            args.addFirst("powershell.exe");
        } else {
            args.addFirst("-c");
            args.addFirst("/bin/sh");
        }


        ProcessBuilder processBuilder = new ProcessBuilder(args);
        StringBuilder logs = runCommand(command, processBuilder, issueKey);

        return logs.toString();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name")
            .toLowerCase().contains("win");
    }

    private StringBuilder runCommand(String command, ProcessBuilder processBuilder, String issueKey) throws IOException, InterruptedException {
        Path workspacePath = workspaceService.getWorkspacePath(issueKey);

        StringBuilder logs = new StringBuilder();

        processBuilder.directory(workspacePath.toFile());
        log.info("Running '{}' inside directory: {}", command, workspacePath.toFile().getAbsolutePath());

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
