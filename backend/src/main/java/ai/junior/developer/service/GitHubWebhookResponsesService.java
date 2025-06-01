package ai.junior.developer.service;

import ai.junior.developer.assistant.AssistantService;
import ai.junior.developer.config.ApplicationPropertiesConfig;
import ai.junior.developer.responses.ResponseIdTracker;
import ai.junior.developer.responses.ResponsesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ai.junior.developer.assistant.AssistantContent.*;
import static ai.junior.developer.service.JiraService.getReplayCommentBody;

@Slf4j
@Service
@AllArgsConstructor
public class GitHubWebhookResponsesService {

    private final ApplicationPropertiesConfig applicationPropertiesConfig;
    private final GitHubService gitHubService;
    private final ResponsesService responsesService;
    private final ObjectMapper objectMapper;
    private final ResponseIdTracker responseIdTracker;

    public void handleWebhook(String payload) throws Exception {
        log.info("Received webhook: {}", payload);

        JsonNode rootNode = objectMapper.readTree(payload);
        JsonNode commentNode = rootNode.get("comment");
        JsonNode prNode = rootNode.get("pull_request");
        JsonNode issueNode = rootNode.get("issue");
        JsonNode finalPrNode = prNode != null ? prNode : issueNode;


        if (commentNode != null && finalPrNode != null) {
            String commentId = commentNode.get("id").toString();
            String commentUserId = commentNode.get("user").get("id").toString();
            String prUserId = finalPrNode.get("user").get("id").toString();
            if (prUserId.equals(applicationPropertiesConfig.getGithub().getUserId())
                && !commentUserId.equals(applicationPropertiesConfig.getGithub().getUserId())) {
                var message = commentNode.get("body").toString();
                JsonNode lineNode = commentNode.get("line");
                JsonNode pathNode = commentNode.get("path");

                log.info("Message: {} is added by another user to pr create the by ai-junior-developer", message);
                var prDescription = finalPrNode.get("body").toString();
                Pattern compile = Pattern.compile("ThreadId:\\[(.*)]");
                Matcher matcher = compile.matcher(prDescription);
                if (matcher.find()) {
                    String threadId = matcher.group(1);
                    if (threadId != null) {
                        log.info("Message: {} was added to thread id: {}", message, threadId);
                        var finalPath = "";
                        if (lineNode != null && pathNode != null) {
                            finalPath = finalPath + "Line: " + lineNode + "\n\r" + "Path: " + pathNode + "\n\r\n\r" + message;
                        } else {
                            finalPath = finalPath + message;
                        }
                        JsonNode prUrlNode = commentNode.get("pull_request_url");
                        JsonNode issueUrlNode = commentNode.get("issue_url");
                        var finalUrl = prUrlNode != null ? prUrlNode.textValue() : issueUrlNode.textValue();
                        var myCommentId = gitHubService.addComment(finalUrl, commentId,
                                JiraResponsesService.getReplayCommentBody(threadId), prUrlNode != null);

                        String responsesId = responseIdTracker.getLastTrackedResponseId();
                        var result = responsesService.createResponses(finalPath, responsesId, threadId);
                        var assistantMessage = responsesService.getOutputListMessages(responsesId);
                        assistantMessage.forEach(mes -> {
                            try {
                                gitHubService.addComment(finalUrl, myCommentId, mes.getMessage(), prUrlNode != null);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        });

                    }
                }

            }
        }
    }

    public void validateRequest(String payload, String xHubSignature) throws NoSuchAlgorithmException, InvalidKeyException {
        final String secret = applicationPropertiesConfig.getGithub().getWebhookSecret();

        final SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        final Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);

        final byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        final HexFormat hex = HexFormat.of();

        final String calculatedSignature = "sha256=" + hex.formatHex(digest);

        if (!MessageDigest.isEqual(calculatedSignature.getBytes(), xHubSignature.getBytes())) {
            log.info("Signatures do not match\nExpected signature:" +
                calculatedSignature + "\nActual: signature: " + xHubSignature);
        } else {
            log.info("Signatures match");
        }
    }
}
