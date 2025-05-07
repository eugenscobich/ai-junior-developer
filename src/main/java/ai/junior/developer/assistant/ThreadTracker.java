package ai.junior.developer.assistant;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ThreadTracker {
    private final List<String> threadIds = new ArrayList<>();

    public synchronized void track(String threadId) {
        threadIds.add(threadId);
    }

    public synchronized List<String> getThreads() {
        return new ArrayList<>(threadIds);
    }
}
