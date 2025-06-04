package ai.junior.developer.service;

import ai.junior.developer.service.llm.LlmService;
import ai.junior.developer.service.llm.assistant.ThreadTracker;
import ai.junior.developer.service.llm.responses.ResponsesTracker;
import ai.junior.developer.service.llm.responses.ResponsesService;
import ai.junior.developer.service.model.*;
import com.openai.client.OpenAIClient;
import com.openai.models.beta.threads.messages.Message;
import com.openai.models.beta.threads.messages.MessageListParams;
import com.openai.models.beta.threads.messages.Text;
import com.openai.models.responses.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import com.openai.models.beta.threads.messages.TextContentBlock;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor
public class ThreadService {

    private final OpenAIClient client;
    private final LlmService llmService;
    private final ThreadTracker threadTracker;
    private final ResponsesTracker responsesTracker;

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

    public Map<String, String> sendPromptToExistingThread(PromptRequest request) {
        MDC.put("assistantId", request.getAssistantId());
        MDC.put("threadId", request.getThreadId());
        String executePrompt = llmService.executePrompt(request.getPrompt(), request.getThreadId());
        return Map.of("assistant_message", executePrompt);
    }

}
