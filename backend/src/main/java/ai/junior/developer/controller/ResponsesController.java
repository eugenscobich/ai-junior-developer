package ai.junior.developer.controller;

import ai.junior.developer.assistant.ThreadTracker;
import ai.junior.developer.responses.ResponseIdTracker;
import ai.junior.developer.responses.ResponsesService;
import ai.junior.developer.service.model.ResponsesByRoleModel;
import ai.junior.developer.service.model.ResponsesItemModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "Responses API", description = "Execute Responses API ")
@RestController
@AllArgsConstructor
public class ResponsesController {

    private final ResponsesService responsesService;
    private final ResponseIdTracker responseIdTracker;
    private final ThreadTracker threadTracker;

    @GetMapping("/api/responses")
    public ResponseEntity<String> executeResponses(@RequestParam String input) throws Exception {
        String responsesId = responseIdTracker.getLastTrackedResponseId();
        String threadId = UUID.randomUUID().toString().replace("-", "");
        threadTracker.track("assistant", threadId);
        var tracked = responsesService.createResponses(input, responsesId, threadId);
        System.out.println("responsesIdList:" + responseIdTracker.getAllTrackedResponsesId());
        log.info(tracked.toString());
        return ResponseEntity.ok(tracked.toString());
    }

    @GetMapping("/api/inputlist/messages")
    public ResponseEntity<ResponsesByRoleModel> getInputListMessages() throws Exception {
        String responsesId = responseIdTracker.getLastTrackedResponseId();
        var tracked = responsesService.getInputListMessages(responsesId);
        return ResponseEntity.ok(tracked);
    }

    @GetMapping("/api/outputlist/messages")
    public ResponseEntity<List<ResponsesItemModel>> getOutputListMessages() throws Exception {
        String responsesId = responseIdTracker.getLastTrackedResponseId();
        var tracked = responsesService.getOutputListMessages(responsesId);
        return ResponseEntity.ok(tracked);
    }
}
