package ai.junior.developer.service;

import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_DESCRIPTION;
import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_INSTRUCTIONS;
import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_MODEL;
import static ai.junior.developer.assistant.AssistantContent.ASSISTANT_NAME;

import ai.junior.developer.assistant.AssistantService;
import ai.junior.developer.config.ApplicationPropertiesConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class GitHubWebhookService {

    private final ApplicationPropertiesConfig applicationPropertiesConfig;
    private final GitHubService gitHubService;
    private final ObjectMapper objectMapper;
    private final AssistantService assistantService;

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
                        var assistant = assistantService.findOrCreateAssistant(
                            AssistantService.buildAssistantParams(
                                ASSISTANT_MODEL, ASSISTANT_NAME,
                                ASSISTANT_DESCRIPTION, ASSISTANT_INSTRUCTIONS
                            ));


                        var finalPath = "";
                        if (lineNode != null && pathNode != null) {
                            finalPath = finalPath + "Line: " + lineNode + "\n\r" + "Path: " + pathNode + "\n\r\n\r" + message;
                        } else {
                            finalPath = finalPath + message;
                        }

                        var result = assistantService.executePrompt(finalPath, assistant.id(), threadId);
                        JsonNode prUrlNode = commentNode.get("pull_request_url");
                        JsonNode issueUrlNode = commentNode.get("issue_url");
                        var finalUrl = prUrlNode != null ? prUrlNode.textValue() : issueUrlNode.textValue();


                        gitHubService.addComment(finalUrl, commentId, result, prUrlNode != null);
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