package ai.junior.developer.service.llm.assistant;

import ai.junior.developer.service.model.ResponsesPromptResponseModel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RunIdTracker {
    private final Map<String, String> promptByRunId = new HashMap<>();
    private final Map<String, ResponsesPromptResponseModel> responsesByRunId = new HashMap<>();

    public synchronized void track(String runId, String userPrompt) {
        promptByRunId.put(runId, userPrompt);
        responsesByRunId.put(runId, ResponsesPromptResponseModel.builder()
            .userPrompt(userPrompt)
            .response("")
            .build());
    }

    public synchronized void trackPromptAndResponse(String runId, String responseMsg) {
        ResponsesPromptResponseModel responsesPromptResponseModel = responsesByRunId.get(runId);
        if (responsesPromptResponseModel != null) {
            responsesByRunId.put(runId, ResponsesPromptResponseModel.builder()
                    .userPrompt(responsesPromptResponseModel.getUserPrompt())
                    .response(responseMsg)
                    .build());
        } else {
            throw new IllegalArgumentException("Run ID not found: " + runId);
        }
    }

    public synchronized Map<String, String> getAllTrackedRunId() {
        return promptByRunId;
    }

    public synchronized Map<String, ResponsesPromptResponseModel> getResponsesByRunId() {
        return responsesByRunId;
    }
}
