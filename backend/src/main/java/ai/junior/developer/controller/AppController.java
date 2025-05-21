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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home Operations", description = "Endpoints to manage home page")
@RestController
@AllArgsConstructor
public class AppController {

    @Operation(
        operationId = "home",
        summary = "Home page",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK")
        }
    )
    @GetMapping({"/{path:^(?!api|static).*}/**"})
    public ResponseEntity<Resource> forward() {
        Resource indexHtml = new ClassPathResource("static/index.html");

        return ResponseEntity
            .ok()
            .contentType(MediaType.TEXT_HTML)
            .body(indexHtml);
    }
}
