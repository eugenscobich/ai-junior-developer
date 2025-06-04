package ai.junior.developer.config.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LlmConfig {

    @NotBlank
    private String type;
}
