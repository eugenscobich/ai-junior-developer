package ai.junior.developer.controller;

import ai.junior.developer.assistant.ThreadTracker;
import ai.junior.developer.service.ThreadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ResponseEntity<List<String>> getThreads(@PathVariable String assistantId) {
        return ResponseEntity.ok(threadTracker.getThreads());
    }

    @GetMapping("/api/messages/{threadId}")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getMessages(@PathVariable String threadId) {
        var messages = threadService.getMessages(threadId);
        return ResponseEntity.ok(messages);
    }
}
