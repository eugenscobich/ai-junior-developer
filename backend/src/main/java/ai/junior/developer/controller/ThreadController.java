package ai.junior.developer.controller;

import ai.junior.developer.service.llm.assistant.RunIdTracker;
import ai.junior.developer.config.ApplicationPropertiesConfig;
import ai.junior.developer.log.LogbackAppender;
import ai.junior.developer.service.ThreadService;
import ai.junior.developer.service.model.MessagesResponse;
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

import java.io.IOException;
import java.util.Map;
import java.util.Queue;

@Tag(name = "Control existing thread", description = "Expose thread id and messages from thread.")
@RestController
@AllArgsConstructor
public class ThreadController {

    private final ThreadService threadService;
    private final RunIdTracker runIdTracker;
    private final LogbackAppender logbackAppender;
    private final ApplicationPropertiesConfig config;

    @GetMapping("/api/threads")
    public ResponseEntity<ThreadsResponse> getThreads() throws Exception {
        ThreadsResponse tracked = threadService.getThreads();
        return ResponseEntity.ok(tracked);
    }

    @GetMapping("/api/messages/{threadId}")
    public ResponseEntity<MessagesResponse> getMessages(@PathVariable String threadId) throws IOException {
        MessagesResponse messages;
        messages = threadService.getMessages(threadId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/api/runIdTrack")
    public ResponseEntity<Map<String, String>> runIdTrack() {
        var runId = runIdTracker.getAllTrackedRunId();
        return ResponseEntity.ok(runId);
    }

    @GetMapping("/api/logs")
    public ResponseEntity<Map<String, Queue<String>>> getLogs() throws Exception {
        Map<String, Queue<String>> logs;
        logs = logbackAppender.getLogMessages();
        return ResponseEntity.ok(logs);
    }

    @PostMapping("/api/prompt/thread")
    public ResponseEntity<Map<String, String>> sendPromptToExistingThread(@RequestBody PromptRequest request) throws Exception {
        Map<String, String> response;
        response = threadService.sendPromptToExistingThread(request);
        return ResponseEntity.ok(response);
    }
}
