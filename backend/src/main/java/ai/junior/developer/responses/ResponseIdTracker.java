package ai.junior.developer.responses;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ResponseIdTracker {
    private final List<String> responsesIdList = new ArrayList<>();

    public synchronized void track(String runId) {
        responsesIdList.add(runId);
    }

    public synchronized List<String> getAllTrackedResponsesId() {
        return responsesIdList;
    }

    public synchronized String getLastTrackedResponseId() {
        var responsesList = getAllTrackedResponsesId();
        return !responsesList.isEmpty() ? responsesList.getLast() : null;
    }
}
