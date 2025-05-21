package ai.junior.developer.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class JiraConfig {

    @Bean
    public RestTemplate jiraRestTemplate(RestTemplateBuilder restTemplateBuilder, ApplicationPropertiesConfig applicationPropertiesConfig) {
        var jira = applicationPropertiesConfig.getJira();
        return restTemplateBuilder
            .rootUri(jira.getBaseUrl())
            .basicAuthentication(jira.getUsername(), jira.getApiToken())
            .build();
    }
}
