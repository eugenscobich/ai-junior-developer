package ai.junior.developer.config;

import ai.junior.developer.config.model.GithubConfig;
import ai.junior.developer.config.model.JiraConfig;
import ai.junior.developer.config.model.LlmConfig;
import ai.junior.developer.config.model.WorkspaceConfig;
import ai.junior.developer.config.model.OllamaConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@ConfigurationProperties(prefix = "service")
@AllArgsConstructor
public class ApplicationPropertiesConfig {

    @NestedConfigurationProperty
    private final WorkspaceConfig workspace;

    @NestedConfigurationProperty
    private final JiraConfig jira;

    @NestedConfigurationProperty
    private final GithubConfig github;

    @NestedConfigurationProperty
    private final LlmConfig llm;

    @NestedConfigurationProperty
    private final OllamaConfig ollama;

}
