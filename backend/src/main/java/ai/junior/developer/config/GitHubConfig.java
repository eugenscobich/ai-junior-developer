package ai.junior.developer.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GitHubConfig {

    @Bean
    public RestTemplate githubRestTemplate(RestTemplateBuilder restTemplateBuilder, ApplicationPropertiesConfig applicationPropertiesConfig) {
        var github = applicationPropertiesConfig.getGithub();
        return restTemplateBuilder
            .rootUri("https://api.github.com")
            .defaultHeader("Authorization", "Bearer " + github.getApiToken())
            .defaultHeader("Accept", "application/vnd.github+json")
            .build();
    }
}
