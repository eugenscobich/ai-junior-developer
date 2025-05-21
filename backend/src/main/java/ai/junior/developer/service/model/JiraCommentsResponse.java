package ai.junior.developer.service.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * POJO for the paged response returned by
 * <pre>GET /rest/api/3/issue/{issueKey}/comment</pre>
 *
 * Matches the sample payload dated 2025‑05‑06 (Jira Cloud v3).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraCommentsResponse {

    private List<Comment> comments;
    private int maxResults;
    private int startAt;
    private int total;

    /* ───────────────── Comment ───────────────── */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comment {
        private String id;
        private User author;
        private User updateAuthor;
        private Body body;
        private String created;
        private String updated;
        @JsonProperty("jsdPublic")
        private boolean jsdPublic;
        private String self;
    }

    /* ───────────────── Body (ADF) ───────────────── */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private String type;   // "doc"
        private int version;   // 1
        private List<ContentBlock> content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentBlock {
        private String type;              // "paragraph"
        private List<TextNode> content;   // inner content nodes
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextNode {
        private String type;  // "text"
        private String text;
    }

    /* ───────────────── User ───────────────── */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String accountId;
        private String accountType;
        private boolean active;
        private Map<String, String> avatarUrls;
        private String displayName;
        private String emailAddress;
        private String self;
        private String timeZone;
    }
}

