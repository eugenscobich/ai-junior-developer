package ai.junior.developer.controller.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse {
    String errorMessage;
}
