package ai.junior.developer.service.model;

import ai.junior.developer.service.ThreadService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class UserOrAssistantMessageResponse {
    public String value;
    public Long createdAt;
}
