package ai.junior.developer.service.llm.responses;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ResponsesTracker {
    private final Map<String, List<String>> responsesTrakerMap = new HashMap<>();

    public synchronized void addAThread(String threadId) {
        responsesTrakerMap.computeIfAbsent(threadId, k -> new ArrayList<>());
    }

    public synchronized void track(String threadId, String responsesId) {
        responsesTrakerMap.computeIfAbsent(threadId, k -> new ArrayList<>()).add(responsesId);
    }

    public synchronized List<String> getAllTrackedResponsesId(String threadId) {
        return responsesTrakerMap.get(threadId);
    }

    public synchronized String getLastTrackedResponseId(String threadId) {
        var responsesList = getAllTrackedResponsesId(threadId);
        return !responsesList.isEmpty() ? responsesList.getLast() : null;
    }
}
