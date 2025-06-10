package ai.junior.developer.service.llm.devstral;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import org.springframework.stereotype.Component;

@Component
public class ChatTracker {
    private final Map<String, List<ResponsesThreadTracker>> responsesThreadTrakerMap = new HashMap<>();
    private final List <String> threadIdList = new ArrayList<>();

    public synchronized void addAThread(String threadId) {
        responsesThreadTrakerMap.computeIfAbsent(threadId, k -> new ArrayList<>());
        threadIdList.add(threadId);
    }

    public synchronized void track(String threadId, String runId, String responsesId) {
        List<ResponsesThreadTracker> trackers =
                responsesThreadTrakerMap.computeIfAbsent(threadId, k -> new ArrayList<>());
        threadIdList.add(threadId);
        ResponsesThreadTracker tracker = trackers.stream()
                .filter(t -> runId.equals(t.getRunId()))
                .findFirst()
                .orElseGet(() -> {
                    ResponsesThreadTracker t = ResponsesThreadTracker.builder()
                            .runId(runId)
                            .build();
                    trackers.add(t);
                    return t;
                });

        tracker.getResponsesIds().add(responsesId);
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

    public List<ResponsesThreadTracker> getThreadDetails(String threadId) {
        return responsesThreadTrakerMap.get(threadId);
    }

    public Boolean isEmptyThreadList() {
        return threadIdList.isEmpty();
    }

    public String getLastThreadId() {
        return threadIdList.getLast();
    }

    @Value
    @Builder
    public static class ResponsesThreadTracker {
        String runId;
        @Builder.Default
        List<String> responsesIds = new ArrayList<>();
    }
}
