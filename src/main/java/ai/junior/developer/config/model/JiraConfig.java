package ai.junior.developer.config.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JiraConfig {

    @NotBlank
    private String webhookSecret;
    @NotBlank
    private String baseUrl;
    @NotBlank
    private String username;
    @NotBlank
    private String apiToken;
}
