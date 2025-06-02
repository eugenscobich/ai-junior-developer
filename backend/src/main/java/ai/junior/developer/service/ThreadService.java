package ai.junior.developer.service;

import ai.junior.developer.assistant.AssistantService;
import ai.junior.developer.assistant.ThreadTracker;
import ai.junior.developer.log.LogbackAppender;
import ai.junior.developer.responses.ResponseIdTracker;
import ai.junior.developer.responses.ResponsesService;
import ai.junior.developer.service.model.*;
import com.openai.client.OpenAIClient;
import com.openai.models.beta.threads.Thread;
import com.openai.models.beta.threads.messages.Message;
import com.openai.models.beta.threads.messages.MessageListParams;
import com.openai.models.beta.threads.messages.Text;
import com.openai.models.responses.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import com.openai.models.beta.threads.messages.TextContentBlock;

import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_DESCRIPTION;
import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_INSTRUCTIONS;
import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_MODEL;
import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_NAME;
import static com.openai.models.beta.threads.messages.Message.Role.Value.ASSISTANT;
import static com.openai.models.beta.threads.messages.Message.Role.Value.USER;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor
public class ThreadService {

    private final OpenAIClient client;
    private final AssistantService assistantService;
    private final ResponsesService responsesService;
    private final ThreadTracker threadTracker;
    private final ResponseIdTracker responseIdTracker;

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

    public MessagesResponse getMessagesFromResponses(String threadId) throws IOException {
        List<String> responsesIds = responseIdTracker.getAllTrackedResponsesId();

        List<MessageResponse> allResponses = new ArrayList<>();

        for (String responsesId : responsesIds) {
            ResponsesByRoleModel trackedUser = responsesService.getInputListMessages(responsesId);
            List<ResponsesItemModel> trackedAssistant = responsesService.getOutputListMessages(responsesId);

            if (trackedUser.getUser().isEmpty() || trackedAssistant.isEmpty()) {
                continue;
            }

            String fullUserMsgId = trackedUser.getUser().get(0).getMessageId();
            String fullAssistantMsgId = trackedAssistant.get(0).getMessageId();

            String userMessageIdExtract = StringUtils.left(
                    StringUtils.substringAfter(fullUserMsgId, "_"), 6);
            String assistantMessageIdExtract = StringUtils.left(
                    StringUtils.substringAfter(fullAssistantMsgId, "_"), 6);

            if (userMessageIdExtract.equals(assistantMessageIdExtract)) {
                UserOrAssistantMessageResponse userMsgDto =
                        UserOrAssistantMessageResponse.builder()
                                .threadId(threadId)
                                .value(trackedUser.getUser().get(0).getMessage())
                                .createdAt(trackedUser.getUser().get(0).getCreatedAt())
                                .runId(userMessageIdExtract)
                                .build();

                boolean hasFunctionCallTool = trackedAssistant.stream()
                        .anyMatch(item -> "functionToolCall".equals(item.getType()));
                Stream<ResponsesItemModel> chosenStream;
                if (hasFunctionCallTool) {
                    chosenStream = trackedUser.getFunctionCall().stream();
                } else {
                    chosenStream = trackedAssistant.stream()
                            .filter(item -> "assistant".equals(item.getType()));
                }
                List<UserOrAssistantMessageResponse> assistantDtos =
                        chosenStream.map(item -> UserOrAssistantMessageResponse.builder()
                                        .threadId(threadId)
                                        .value(item.getMessage())
                                        .createdAt(item.getCreatedAt())
                                        .runId(assistantMessageIdExtract)
                                        .build()
                                )
                                .toList();

                allResponses.add(
                        MessageResponse.builder()
                                .userMessage(userMsgDto)
                                .assistantMessages(assistantDtos)
                                .build()
                );
            }
        }

        return MessagesResponse.builder()
                .messagesList(allResponses)
                .build();
    }

    public Map<String, Queue<String>> getFunctionCall() throws Exception {
        String uuid = getThreads().getThreadId();
        List<String> responsesIdId = responseIdTracker.getAllTrackedResponsesId();
        Map<String, Queue<String>> functionCall = new HashMap<>();

        for (String responsesId : responsesIdId) {
            List<ResponsesItemModel> trackedAssistant = responsesService.getOutputListMessages(responsesId);

            if (trackedAssistant.isEmpty()) {
                continue;
            }

            String key = uuid + "_" + responsesId;
            boolean hasFunctionCallTool = trackedAssistant.stream()
                    .anyMatch(item -> "functionToolCall".equals(item.getType()));

            if (hasFunctionCallTool) {
                List<ResponsesItemModel> chosenStream = trackedAssistant.stream()
                        .filter(item -> "functionToolCall".equals(item.getType())).toList();
                Queue<String> queue = new LinkedList<>();
                chosenStream.forEach(item -> queue.add(item.getMessage()));
                functionCall.put(key, queue);
            }
        }
        return functionCall;
    }

    public Map<String, String> sendPromptToExistingThread(PromptRequest request) throws Exception {
        MDC.put("assistantId", request.getAssistantId());
        MDC.put("threadId", request.getThreadId());
        return assistantService.executePrompt(request.getPrompt(), request.getAssistantId(), request.getThreadId());
    }

    public Map<String, String> sendPromptToResponses(PromptRequest request) throws Exception {
        var previousResponses = responseIdTracker.getLastTrackedResponseId();
        Response response = responsesService.createResponses(request.getPrompt(), previousResponses, request.getThreadId());

        return responsesService.getAssistantMessage(response.id());
    }
}
