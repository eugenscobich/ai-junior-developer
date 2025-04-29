package ai.junior.developer.controller;

import ai.junior.developer.service.MavenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Maven Operations", description = "Running clean install to detect any error.")
@RestController
@RequestMapping("/api/build")
@AllArgsConstructor
public class MavenController {

    private final MavenService mavenService;

    @Operation(summary = "Perform action maven clean install")
    @GetMapping("/run")
    public String runCleanInstall(@RequestParam String project) {
        return mavenService.runCleanInstall(project);
    }

    @Operation(summary = "Perform action maven test")
    @GetMapping("/runtests")
    public String runTests(@RequestParam String project) {
        return mavenService.runTests(project);
    }
}
