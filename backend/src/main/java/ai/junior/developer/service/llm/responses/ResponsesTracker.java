package ai.junior.developer.service.llm.responses;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ResponsesTracker {
    private final Map<String, List<ResponsesThreadTracker>> responsesThreadTrakerMap = new HashMap<>();

    public synchronized void addAThread(String threadId) {
        responsesThreadTrakerMap.computeIfAbsent(threadId, k -> new ArrayList<>());
    }

    public synchronized void track(String threadId, String runId, String responsesId) {
        var responsesThreadTracker = responsesThreadTrakerMap.get(threadId).stream()
            .filter(rtt -> rtt.getRunId().equals(runId)).findFirst()
            .orElseGet(() -> ResponsesThreadTracker.builder()
                .build());

        responsesThreadTracker.getResponsesIds().add(responsesId);
    }

    public synchronized String getLastTrackedResponseId(String threadId) {
        List<ResponsesThreadTracker> responsesThreadTrackers = responsesThreadTrakerMap.get(threadId);
        if (responsesThreadTrackers == null || responsesThreadTrackers.isEmpty()) {
            return null;
        } else {
            return responsesThreadTrackers.getLast().getResponsesIds().isEmpty() ?
                null : responsesThreadTrackers.getLast().getResponsesIds().getLast();
        }
    }

    @Value
    @Builder
    private static class ResponsesThreadTracker {
        String runId;
        @Builder.Default
        List<String> responsesIds = new ArrayList<>();
    }
}
