package ai.junior.developer.config.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GithubConfig {
    @NotBlank
    private String apiToken;
    @NotBlank
    private String webhookSecret;
    @NotBlank
    private String userId;
}
