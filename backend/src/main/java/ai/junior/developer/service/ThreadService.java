package ai.junior.developer.service;

import ai.junior.developer.service.llm.LlmService;
import ai.junior.developer.service.llm.assistant.RunIdTracker;
import ai.junior.developer.service.llm.assistant.ThreadTracker;
import ai.junior.developer.service.llm.responses.ResponsesService;
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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ThreadService {

    private final LlmService llmService;

    public ThreadsResponse getThreads() {
        return llmService.getLastThread();
    }

    public MessagesResponse getMessages(String threadId) throws IOException {
        return llmService.getThreadMessages(threadId);
    }

    public Map<String, String> sendPromptToExistingThread(PromptRequest request) throws Exception {
        return llmService.sendPromptToExistingThread(request);
    }
}
