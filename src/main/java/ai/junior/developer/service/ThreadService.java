package ai.junior.developer.service;

import com.openai.client.OpenAIClient;
import com.openai.models.beta.threads.messages.Message;
import com.openai.models.beta.threads.messages.MessageListParams;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.openai.models.beta.threads.messages.Message.Role.Value.ASSISTANT;
import static com.openai.models.beta.threads.messages.Message.Role.Value.USER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class ThreadService {

    private final OpenAIClient client;

    public Map<String, List<Map<String, Object>>> getMessages(String threadId) {
        List<Message> allMessages = client.beta().threads().messages().list(
                MessageListParams.builder().threadId(threadId).build()).data();

        List<Map<String, Object>> user = new ArrayList<>();
        List<Map<String, Object>> assistant = new ArrayList<>();

        for (Message msg : allMessages) {
            Map<String, Object> item = Map.of(
                    "content", msg.content().toString(),
                    "created", msg.createdAt()
            );
            switch (msg.role()) {
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
}
