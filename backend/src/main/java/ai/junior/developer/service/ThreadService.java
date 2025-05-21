package ai.junior.developer.service;

import ai.junior.developer.assistant.AssistantService;
import ai.junior.developer.assistant.ThreadTracker;
import ai.junior.developer.log.LogbackAppender;
import ai.junior.developer.service.model.MessageResponse;
import ai.junior.developer.service.model.MessagesResponse;
import ai.junior.developer.service.model.PromptRequest;
import ai.junior.developer.service.model.ThreadsResponse;
import ai.junior.developer.service.model.UserOrAssistantMessageResponse;
import com.openai.client.OpenAIClient;
import com.openai.models.beta.threads.Thread;
import com.openai.models.beta.threads.messages.Message;
import com.openai.models.beta.threads.messages.MessageListParams;
import com.openai.models.beta.threads.messages.Text;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import com.openai.models.beta.threads.messages.TextContentBlock;

import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_DESCRIPTION;
import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_INSTRUCTIONS;
import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_MODEL;
import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_NAME;
import static com.openai.models.beta.threads.messages.Message.Role.Value.ASSISTANT;
import static com.openai.models.beta.threads.messages.Message.Role.Value.USER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    public MessagesResponse getMessages(String threadId) {
        List<Message> allMessages = client.beta().threads().messages().list(
                MessageListParams.builder().threadId(threadId).build()).data();

        allMessages.sort(Comparator.comparing(Message::createdAt));

        Map<String, MessageResponse> groupedMessages = new LinkedHashMap<>();
        String currentUserMessageId = null;

        for (Message msg : allMessages) {
            String value = msg.content().stream()
                    .map(c -> c.text()
                            .map(TextContentBlock::text)
                            .map(Text::value)
                            .orElse("[empty]"))
                    .collect(Collectors.joining("\n"));

            UserOrAssistantMessageResponse messageDto = UserOrAssistantMessageResponse.builder()
                    .value(value)
                    .createdAt(msg.createdAt())
                    .threadId(msg.threadId())
                    .runId(msg.runId().isPresent() ? msg.runId().get() : null)
                    .build();

            switch (msg.role().value()) {
                case USER -> {
                    currentUserMessageId = msg.id();
                    MessageResponse response = MessageResponse.builder()
                            .userMessage(messageDto)
                            .assistantMessages(new ArrayList<>())
                            .build();
                    groupedMessages.put(currentUserMessageId, response);
                }
                case ASSISTANT -> {
                    if (currentUserMessageId != null) {
                        groupedMessages.get(currentUserMessageId).getAssistantMessages().add(messageDto);
                    } else {
                        log.warn("no user message before: {}", msg);
                    }
                }
                default -> {
                    log.warn("Unhandled role: {}", msg.role());
                }
            }
        }

        return MessagesResponse.builder()
                .messagesList(new ArrayList<>(groupedMessages.values()))
                .build();
    }

    public Map<String, String> sendPromptToExistingThread(PromptRequest request) throws Exception {
        MDC.put("assistantId", request.getAssistantId());
        MDC.put("threadId", request.getThreadId());
        return assistantService.executePrompt(request.getPrompt(), request.getAssistantId(), request.getThreadId());
    }
}
