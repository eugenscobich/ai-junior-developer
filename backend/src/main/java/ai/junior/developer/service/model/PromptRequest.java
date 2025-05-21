package ai.junior.developer.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromptRequest {
    public String assistantId;
    public String threadId;
    public String prompt;
}
