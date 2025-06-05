package ai.junior.developer.service.llm;

import ai.junior.developer.service.model.MessagesResponse;

public interface LlmService {

    String startAThread();

    void continueAThread(String threadId);

    String executePrompt(String prompt, String threadId);

    void getLastThreadId();

    MessagesResponse getThreadMessages(String threadId);

}
