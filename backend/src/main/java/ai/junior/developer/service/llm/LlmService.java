package ai.junior.developer.service.llm;

import ai.junior.developer.service.model.MessagesResponse;
import ai.junior.developer.service.model.PromptRequest;
import ai.junior.developer.service.model.ThreadsListModel;
import ai.junior.developer.service.model.ThreadsResponse;

import java.io.IOException;
import java.util.Map;

public interface LlmService {

    String startAThread();

    void continueAThread(String threadId);

    String executeLlmPrompt(String prompt, String threadId);

    MessagesResponse getThreadMessages(String threadId) throws IOException;

    Map<String, String>  sendPromptToExistingThread(PromptRequest request);

    ThreadsResponse getLastThread();

    ThreadsListModel getAllThreads();
}
