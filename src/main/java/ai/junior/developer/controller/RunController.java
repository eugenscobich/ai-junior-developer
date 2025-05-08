package ai.junior.developer.controller;

import ai.junior.developer.service.MavenService;
import ai.junior.developer.service.RunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Local Run Operations", description = "Running local command to detect any error.")
@RestController
@RequestMapping("/api/run")
@AllArgsConstructor
public class RunController {

    private final RunService runService;

    @Operation(summary = "Perform local run")
    @GetMapping("/")
    public String run(@RequestParam String command) throws IOException, InterruptedException {
        return runService.run(command);
    }
}
