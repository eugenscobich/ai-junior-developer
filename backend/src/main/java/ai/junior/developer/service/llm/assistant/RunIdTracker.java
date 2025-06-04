package ai.junior.developer.service.llm.assistant;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RunIdTracker {
    private final Map<String, String> promptByRunId = new HashMap<>();

    public synchronized void track(String runId, String userPrompt) {
        promptByRunId.put(runId, userPrompt);
    }

    public synchronized Map<String, String> getAllTrackedRunId() {
        return promptByRunId;
    }
}
