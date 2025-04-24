package ai.junior.developer.controller.advice;

import ai.junior.developer.controller.model.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@org.springframework.web.bind.annotation.ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUnknownException(RuntimeException ex) {
        log.error("Error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
            .body(ErrorResponse.builder()
                .errorMessage(ex.getMessage())
                .build());
    }

}
