package ai.junior.developer.service;

import ai.junior.developer.assistant.AssistantService;
import ai.junior.developer.assistant.ThreadTracker;
import ai.junior.developer.service.model.PromptRequest;
import ai.junior.developer.service.model.ThreadsResponse;
import com.openai.client.OpenAIClient;
import com.openai.models.beta.threads.Thread;
import com.openai.models.beta.threads.messages.Message;
import com.openai.models.beta.threads.messages.MessageListParams;
import com.openai.models.beta.threads.messages.Text;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.openai.models.beta.threads.messages.TextContentBlock;

import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_DESCRIPTION;
import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_INSTRUCTIONS;
import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_MODEL;
import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_NAME;
import static com.openai.models.beta.threads.messages.Message.Role.Value.ASSISTANT;
import static com.openai.models.beta.threads.messages.Message.Role.Value.USER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ThreadService {

    private final OpenAIClient client;
    private final AssistantService assistantService;
    private final ThreadTracker threadTracker;

    public ThreadsResponse getThreads() throws Exception {
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

    public Map<String, Object> getMessages(String threadId) {
        List<Message> allMessages = client.beta().threads().messages().list(
                MessageListParams.builder().threadId(threadId).build()).data();

        Map<String, Object> userMessage = null;
        List<Map<String, Object>> assistantMessages = new ArrayList<>();

        for (Message msg : allMessages) {
            String value = msg.content().stream()
                    .map(c -> c.text()
                            .map(TextContentBlock::text)
                            .map(Text::value)
                            .orElse("[empty]"))
                    .collect(Collectors.joining("\n"));

            Map<String, Object> item = Map.of(
                    "content", value,
                    "created", msg.createdAt()
            );
            switch (msg.role().value()) {
                case USER -> userMessage = item;
                case ASSISTANT -> assistantMessages.add(item);
                default -> {
                    log.warn("Unhandled role: {}", msg.role());
                }
            }
        }

        if (userMessage != null) {
            return Map.of(
                    "userMessages", userMessage,
                    "assistantMessages", assistantMessages
            );
        } else {
            return Collections.emptyMap();
        }
    }

    public String sendPromptToExistingThread(PromptRequest request) throws Exception {
        System.out.println(request);

        return assistantService.executePrompt(request.getPrompt(), request.getAssistantId(), request.getThreadId());
    }
}
