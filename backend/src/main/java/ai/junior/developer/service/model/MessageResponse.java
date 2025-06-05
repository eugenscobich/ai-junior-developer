package ai.junior.developer.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class MessageResponse {
    public String runId;
    public UserOrAssistantMessageResponse userMessage;
    public List<UserOrAssistantMessageResponse> assistantMessages;
}
