package ai.junior.developer.service;

import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_DESCRIPTION;
import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_INSTRUCTIONS;
import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_NAME;

import ai.junior.developer.assistant.AssistantService;
import ai.junior.developer.assistant.ThreadTracker;
import ai.junior.developer.config.ApplicationPropertiesConfig;
import ai.junior.developer.service.model.JiraCommentsResponse;
import ai.junior.developer.service.model.JiraCommentsResponse.Comment;
import ai.junior.developer.service.model.JiraIssue;
import ai.junior.developer.service.model.JiraIssue.User;
import ai.junior.developer.service.model.JiraWebhookEvent;
import ai.junior.developer.service.model.JiraWebhookEvent.Changelog.Item;
import com.atlassian.adf.jackson2.AdfJackson2;
import com.atlassian.adf.markdown.MarkdownParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.models.beta.threads.Thread;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@AllArgsConstructor
public class JiraService {

    private final ApplicationPropertiesConfig applicationPropertiesConfig;

    private final RestTemplate jiraRestTemplate;
    private final ObjectMapper objectMapper;
    private final AssistantService assistantService;
    private final ThreadTracker threadTracker;

    @Async
    public void webhook(String requestBody) throws Exception {
        log.info(requestBody);
        JiraWebhookEvent jiraWebhookEvent = objectMapper.readValue(requestBody, JiraWebhookEvent.class);
        String issueKey = jiraWebhookEvent.getIssue().getKey();

        var issueDetails = getIssueDetails(issueKey);
        var threadId = issueDetails.getFields().getExtras().get(applicationPropertiesConfig.getJira().getTreadIdCustomFieldName());
        var openAiModel = issueDetails.getFields().getExtras().get(applicationPropertiesConfig.getJira().getOpenAiModelCustomFieldName());
        var model = ((LinkedHashMap) openAiModel).get("value").toString();
        var assistant = assistantService.findOrCreateAssistant(
            AssistantService.buildAssistantParams(
                model, ASSISTANT_NAME,
                ASSISTANT_DESCRIPTION, ASSISTANT_INSTRUCTIONS
            ), model
        );
        MDC.put("assistantId", assistant.id());
        if (jiraWebhookEvent.getWebhookEvent().equals("jira:issue_updated")) {
            if (jiraWebhookEvent.getChangelog() != null && !jiraWebhookEvent.getChangelog().getItems().isEmpty()) {
                Optional<Item> assignee = jiraWebhookEvent.getChangelog().getItems().stream().filter(item -> item
                    .getFieldId().equals("assignee")).findFirst();
                if (assignee.isPresent()) {
                    if (assignee.get().getTo() != null && assignee.get().getTo().equals(applicationPropertiesConfig.getJira().getUserId())
                        && threadId == null) {
                        log.info("Ticket was assigned to AI Junior Developer");

                        Thread thread = assistantService.createThread();
                        threadTracker.track(assistant.id(), thread.id());
                        MDC.put("threadId", thread.id());
                        updateFields(issueKey, Map.of(applicationPropertiesConfig.getJira().getTreadIdCustomFieldName(), thread.id()));

                        addComment(
                            issueKey,
                            getReplayCommentBody(assistant.id(), thread.id())
                        );
                        var result = assistantService.executePrompt(
                            "Issue key: " + issueKey + "\n"
                                + "Task title: " + issueKey + "-" + jiraWebhookEvent.getIssue().getFields().getSummary() + "\n"
                                + "Task: " + jiraWebhookEvent.getIssue().getFields().getDescription(), assistant.id(), thread.id()
                        );

                        addComment(issueKey, result.get("assistantMessage"));
                    }
                }
            }
        } else if (jiraWebhookEvent.getWebhookEvent().equals("comment_updated") || jiraWebhookEvent.getWebhookEvent().equals("comment_created")) {
            log.info("Comment is added to ticket that have trace id {}", threadId);
            User assignee = issueDetails.getFields().getAssignee();
            if (assignee != null
                && threadId != null
                && !Objects.equals(jiraWebhookEvent.getComment().getAuthor().getAccountId(), applicationPropertiesConfig.getJira().getUserId())
                && assignee.getAccountId().equals(applicationPropertiesConfig.getJira().getUserId())
            ) {
                log.info("Comment is addressed to ai-junior-developer");
                var result = assistantService.executePrompt(jiraWebhookEvent.getComment().getBody(), assistant.id(), threadId.toString());
                addComment(issueKey, result.get("assistantMessage"));
            }

        }
    }

    public static String getReplayCommentBody(String assistantId, String threadId) {
        return "Task was assigned to AI Junior Developer.\n"
            + "Links:\n[AI Junior Developer Console](http://localhost:3000/" + threadId + "/messages)\n"
            + "[OpenAI Platform](https://platform.openai.com/playground/assistants?assistant=" + assistantId + "&thread=" + threadId + ")\n";
    }

    /**
     * Updates fields of the given Jira issue.
     *
     * @param issueKey the key of the issue, e.g. <code>"PROJ-123"</code>
     * @param fields   map of fields and their new values
     */
    public void updateFields(String issueKey, Map<String, Object> fields) {
        // Payload creation
        Map<String, Object> payload = Map.of("fields", fields);

        // Prepare HTTP request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // Jira Cloud REST v3 endpoint for issue updates
        String url = "/rest/api/3/issue/{issueKey}";

        // Execute PUT request and ignore the response
        jiraRestTemplate.put(url, entity, issueKey);
    }

    public void validateRequest(String requestBody, String xHubSignature) throws NoSuchAlgorithmException, InvalidKeyException {
        final String secret = applicationPropertiesConfig.getJira().getWebhookSecret();

        final SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        final Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);

        final byte[] digest = mac.doFinal(requestBody.getBytes(StandardCharsets.UTF_8));
        final HexFormat hex = HexFormat.of();

        final String calculatedSignature = "sha256=" + hex.formatHex(digest);

        if (!MessageDigest.isEqual(calculatedSignature.getBytes(), xHubSignature.getBytes())) {
            log.info("Signatures do not match\nExpected signature:" +
                calculatedSignature + "\nActual: signature: " + xHubSignature);
        } else {
            log.info("Signatures match");
        }
    }

    /**
     * Adds a comment to the given Jira issue.
     *
     * @param issueKey    the key of the issue, e.g. <code>"PROJ-123"</code>
     * @param commentBody plain‑text comment body
     */
    public void addComment(String issueKey, String commentBody) throws JsonProcessingException {
        if (commentBody.isEmpty()) {
            log.warn("Comment is empty");
            return;
        }
        var markdownParser = new MarkdownParser();

        var result = markdownParser.unmarshall(commentBody);

        AdfJackson2 adfJackson2 = new AdfJackson2();

        String body = adfJackson2.marshall(result);

        JsonNode jsonNode = objectMapper.readTree(body);

        Map<String, Object> payload = Map.of("body", jsonNode);

        // --- Prepare HTTP request --------------------------------------------------------------
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // Jira Cloud REST v3 endpoint for comments
        String url = "/rest/api/3/issue/{issueKey}/comment";

        // Execute POST and ignore the response (URI of the new comment), or capture if needed.
        jiraRestTemplate.postForLocation(url, entity, issueKey);
    }

    /**
     * Retrieves <strong>all</strong> comments on {@code issueKey} that were authored by the user
     * whose Atlassian {@code accountId} matches the supplied value.
     * <p>
     * Jira’s GET <code>/comment</code> endpoint is paginated, so this method loops until all pages
     * are fetched.
     *
     * @param issueKey issue key, e.g. {@code JKNIG-1}
     */
    public List<Comment> getComments(String issueKey) {
        final int pageSize = 100;
        int startAt = 0;
        List<Comment> result = new ArrayList<>();

        while (true) {
            JiraCommentsResponse page = jiraRestTemplate.getForObject(
                "/rest/api/3/issue/{issueKey}/comment?startAt={startAt}&maxResults={maxResults}",
                JiraCommentsResponse.class,
                issueKey,
                startAt,
                pageSize
            );

            if (page == null) {
                break; // defensive
            }

            int total = page.getTotal();
            var comments = page.getComments();
            for (var c : comments) {
                var author = c.getAuthor();
                if (author != null && applicationPropertiesConfig.getJira().getUserId().equals(author.getAccountId())) {
                    result.add(c);
                }
            }

            // Pagination bookkeeping
            startAt += comments.size();
            if (startAt >= total) {
                break;
            }

        }
        return result;
    }

    /**
     * Retrieves detailed information about a specific Jira issue, given its key.
     *
     * @param issueKey the unique key identifying the Jira issue, e.g., "JKNIG-1"
     * @return JSON object containing issue details
     */
    public JiraIssue getIssueDetails(String issueKey) throws JsonProcessingException {
        // URI for Jira issue details
        String url = "/rest/api/3/issue/" + issueKey;

        // Retrieve issue details as a JSON string
        var forObject = jiraRestTemplate.getForEntity(url, String.class);
        log.info(forObject.getBody());
        return objectMapper.readValue(forObject.getBody(), JiraIssue.class);
    }
}
