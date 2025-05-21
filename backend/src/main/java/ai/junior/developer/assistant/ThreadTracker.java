package ai.junior.developer.assistant;

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
}
