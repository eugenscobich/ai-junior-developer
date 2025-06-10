package ai.junior.developer.config;

import io.github.ollama4j.OllamaAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfig {

    @Bean
    public OllamaAPI ollamaApi(ApplicationPropertiesConfig applicationPropertiesConfig) {
        String host = applicationPropertiesConfig.getOllama().getBaseUrl();
        OllamaAPI ollamaAPI = new OllamaAPI(host);
        ollamaAPI.setRequestTimeoutSeconds(6000);
        ollamaAPI.setVerbose(false);
        return ollamaAPI;
    }


}
