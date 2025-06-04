package ai.junior.developer.service.llm;

public interface LlmService {

    String startAThread();

    void continueAThread(String threadId);

    String executePrompt(String prompt, String threadId);

}
