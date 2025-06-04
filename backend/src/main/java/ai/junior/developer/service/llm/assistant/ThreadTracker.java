package ai.junior.developer.service.llm.assistant;

import java.util.UUID;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ThreadTracker {
    private final Map<String, List<String>> threadsByAssistant = new HashMap<>();

    public synchronized void track(String assistantId, String threadId) {
        threadsByAssistant
                .computeIfAbsent(assistantId, k -> new ArrayList<>())
                .add(threadId);
    }

    public synchronized Map<String, List<String>> getAllTracked() {
        return threadsByAssistant;
    }

    public String findAssistantId(String threadId) {
        return threadsByAssistant.entrySet().stream()
            .filter(entry -> entry.getValue().contains(threadId))
            .findFirst()
            .map(Map.Entry::getKey).orElse(null);

    }
}
