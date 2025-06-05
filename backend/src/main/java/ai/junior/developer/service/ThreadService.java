package ai.junior.developer.service;

import ai.junior.developer.service.llm.LlmService;
import ai.junior.developer.service.llm.assistant.ThreadTracker;
import ai.junior.developer.service.llm.responses.ResponsesTracker;
import ai.junior.developer.service.model.*;
import com.openai.client.OpenAIClient;
import com.openai.models.beta.threads.messages.Message;
import com.openai.models.beta.threads.messages.MessageListParams;
import com.openai.models.beta.threads.messages.Text;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import com.openai.models.beta.threads.messages.TextContentBlock;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ThreadService {

    private final OpenAIClient client;
    private final LlmService llmService;
    private final ThreadTracker threadTracker;
    private final ResponsesTracker responsesTracker;

    public ThreadsResponse getThreads() {

        llmService.getLastThreadId();

        Map<String, List<String>> activeThread = threadTracker.getAllTracked();
        System.out.println("activeThread " + activeThread);
        ThreadsResponse tracked = activeThread.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> ThreadsResponse.builder()
                        .assistantId(entry.getKey())
                        .threadId(entry.getValue().get(0))
                        .build())
                .findFirst().get();

        return tracked;
    }

    public MessagesResponse getMessages(String threadId) {
        return llmService.getThreadMessages(threadId);
    }

    public Map<String, String> sendPromptToExistingThread(PromptRequest request) {
        MDC.put("assistantId", request.getAssistantId());
        MDC.put("threadId", request.getThreadId());
        String executePrompt = llmService.executePrompt(request.getPrompt(), request.getThreadId());
        return Map.of("assistant_message", executePrompt);
    }

}
