package ai.junior.developer.service;

import ai.junior.developer.assistant.AssistantService;
import ai.junior.developer.service.model.PromptRequest;
import com.openai.client.OpenAIClient;
import com.openai.models.beta.threads.messages.Message;
import com.openai.models.beta.threads.messages.MessageListParams;
import com.openai.models.beta.threads.messages.Text;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.openai.models.beta.threads.messages.TextContentBlock;

import static com.openai.models.beta.threads.messages.Message.Role.Value.ASSISTANT;
import static com.openai.models.beta.threads.messages.Message.Role.Value.USER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ThreadService {

    private final OpenAIClient client;
    private final AssistantService assistantService;

    public Map<String, List<Map<String, Object>>> getMessages(String threadId) {
        List<Message> allMessages = client.beta().threads().messages().list(
                MessageListParams.builder().threadId(threadId).build()).data();

        List<Map<String, Object>> user = new ArrayList<>();
        List<Map<String, Object>> assistant = new ArrayList<>();

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
                case USER -> user.add(item);
                case ASSISTANT -> assistant.add(item);
                default -> {
                    log.warn("Unhandled role: {}", msg.role());
                }
            }
        }

        return Map.of(
                "userMessages", user,
                "assistantMessages", assistant
        );
    }

    public String sendPromptToExistingThread(PromptRequest request) throws Exception {
        System.out.println(request);

        return assistantService.executePrompt(request.getPrompt(), request.getAssistantId(), request.getThreadId());
    }
}
