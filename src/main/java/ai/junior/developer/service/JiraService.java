package ai.junior.developer.service;

import ai.junior.developer.config.ApplicationPropertiesConfig;
import ai.junior.developer.exception.AiJuniorDeveloperException;
import ai.junior.developer.service.model.JiraWebhookEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@AllArgsConstructor
public class JiraService {

    private final ApplicationPropertiesConfig applicationPropertiesConfig;
    private final RestTemplate jiraRestTemplate;
    private final ObjectMapper  objectMapper;


    public void webhook(String requestBody) throws JsonProcessingException {
        log.info(requestBody);
        JiraWebhookEvent jiraWebhookEvent = objectMapper.readValue(requestBody, JiraWebhookEvent.class);
        addComment(jiraWebhookEvent.getIssue().getKey(), "Web Hook received");
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
    public void addComment(String issueKey, String commentBody) {

        // --- Build ADF document wrapper --------------------------------------------------------
        Map<String, Object> adfBody = Map.of(
            "type", "doc",
            "version", 1,
            "content", List.of(
                Map.of(
                    "type", "paragraph",
                    "content", List.of(
                        Map.of(
                            "type", "text",
                            "text", commentBody
                        )
                    )
                )
            )
        );

        Map<String, Object> payload = Map.of("body", adfBody);

        // --- Prepare HTTP request --------------------------------------------------------------
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // Jira Cloud REST v3 endpoint for comments
        String url = "/rest/api/3/issue/{issueKey}/comment";

        // Execute POST and ignore the response (URI of the new comment), or capture if needed.
        jiraRestTemplate.postForLocation(url, entity, issueKey);
    }
}
