package ai.junior.developer.config.model;

import jakarta.validation.constraints.NotBlank;
import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorkspaceConfig {

    @NotBlank
    private Path path;
}
