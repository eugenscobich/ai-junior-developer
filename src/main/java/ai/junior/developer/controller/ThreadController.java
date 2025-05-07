package ai.junior.developer.controller;

import ai.junior.developer.assistant.ThreadTracker;
import ai.junior.developer.service.ThreadService;
import ai.junior.developer.service.model.PromptRequest;
import ai.junior.developer.service.model.ThreadsResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Control existing thread", description = "Expose thread id and messages from thread.")
@RestController
@AllArgsConstructor
public class ThreadController {

    private final ThreadTracker threadTracker;
    private final ThreadService threadService;

    @GetMapping("/api/threads")
    public ResponseEntity<ThreadsResponse> getThreads() {
        Map<String, List<String>> activeThread= threadTracker.getAllTracked();
        List<ThreadsResponse> tracked = activeThread.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> ThreadsResponse.builder()
                        .assistantId(entry.getKey())
                        .threadId(entry.getValue().get(0))
                        .build())
                .toList();
        return ResponseEntity.ok(tracked.getFirst());
    }

    @GetMapping("/api/messages/{threadId}")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getMessages(@PathVariable String threadId) {
        var messages = threadService.getMessages(threadId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/api/prompt/thread")
    public ResponseEntity<String> sendPromptToExistingThread(@RequestBody PromptRequest request) throws Exception {
        String response = threadService.sendPromptToExistingThread(request);
        return ResponseEntity.ok(response);
    }
}
