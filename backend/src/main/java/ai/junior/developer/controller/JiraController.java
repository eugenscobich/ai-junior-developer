package ai.junior.developer.controller;

import ai.junior.developer.config.ApplicationPropertiesConfig;
import ai.junior.developer.service.JiraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Jira Operations", description = "Endpoints to manage jira webhooks")
@RestController
@RequestMapping("/api/jira")
@AllArgsConstructor
public class JiraController {

    private final JiraService jiraService;
    private final ApplicationPropertiesConfig config;

    @Operation(
            operationId = "webhook",
            summary = "Handle Jira webhook",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "500", description = "Internal Server error")
            }
    )
    @PostMapping("/webhook")
    public void webhook(@RequestBody String requestBody, @RequestHeader("X-Hub-Signature") String xHubSignature)
            throws Exception {
        jiraService.validateRequest(requestBody, xHubSignature);
        jiraService.webhook(requestBody);
    }
}
