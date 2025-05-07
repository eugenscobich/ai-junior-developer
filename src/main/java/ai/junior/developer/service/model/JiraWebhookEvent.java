package ai.junior.developer.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * POJO structure for Jira "issue_updated" webhook payloads (2025-05-06 sample).
 * <p>
 * Uses Lombok {@code @Data} for boilerplate and Jackson for JSON mapping.
 * Unknown/extra fields are ignored so payload changes won’t break deserialization.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraWebhookEvent {

    private Comment comment;
    private Changelog changelog;
    private Issue issue;

    @JsonProperty("issue_event_type_name")
    private String issueEventTypeName;

    private long timestamp;
    private User user;
    private String webhookEvent;

    /* ───────────────── Changelog ───────────────── */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Changelog {

        private String id;
        private List<Item> items;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Item {

            private String field;
            private String fieldId;
            @JsonProperty("fieldtype")
            private String fieldType;
            private String from;
            private String fromString;
            private String to;
            private String toString;
        }
    }

    /* ───────────────── Issue ───────────────── */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Issue {

        private Fields fields;
        private String id;
        private String key;
        private String self;

        /* ───────── Issue.Fields ───────── */
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Fields {

            @JsonProperty("aggregateprogress")
            private Progress aggregateProgress;
            private Long aggregatetimeestimate;
            private Long aggregatetimeoriginalestimate;
            private Long aggregatetimespent;
            private User assignee;
            private List<Object> attachment;
            private List<Object> components;
            private String created;
            private User creator;
            @JsonProperty("customfield_10000")
            private String customField10000;
            @JsonProperty("customfield_10001")
            private Object customField10001;
            @JsonProperty("customfield_10019")
            private String customField10019;
            @JsonProperty("customfield_10021")
            private Object customField10021;
            @JsonProperty("customfield_10059")
            private Object customfield_10059;
            @JsonProperty("customfield_10058")
            private Object customfield_10058;
            private String description;
            private String duedate;
            private String environment;
            private List<Object> fixVersions;
            @JsonProperty("issuelinks")
            private List<Object> issueLinks;
            private IssueRestriction issuerestriction;
            private IssueType issuetype;
            private List<String> labels;
            private String lastViewed;
            private Priority priority;
            private Progress progress;
            private Project project;
            private User reporter;
            private Object resolution;
            private String resolutiondate;
            private Object security;
            private Status status;
            private StatusCategory statusCategory;
            private String statuscategorychangedate;
            private List<Object> subtasks;
            private String summary;
            private Long timeestimate;
            private Long timeoriginalestimate;
            private Long timespent;
            private Map<String, Object> timetracking;
            private String updated;
            private List<Object> versions;
            private Votes votes;
            private Watches watches;
            private Integer workratio;
        }
    }

    /* ───────────────── Shared sub-objects ───────────────── */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Progress {

        private int progress;
        private int total;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {

        private String accountId;
        private String accountType;
        private boolean active;
        private Map<String, String> avatarUrls;
        private String displayName;
        private String self;
        private String timeZone;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueRestriction {

        private Map<String, Object> issuerestrictions;
        private boolean shouldDisplay;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueType {

        private int avatarId;
        private String description;
        private String entityId;
        private int hierarchyLevel;
        private String iconUrl;
        private String id;
        private String name;
        private String self;
        private boolean subtask;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Priority {

        private String iconUrl;
        private String id;
        private String name;
        private String self;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Project {

        private Map<String, String> avatarUrls;
        private String id;
        private String key;
        private String name;
        private String projectTypeKey;
        private String self;
        private boolean simplified;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {

        private String description;
        private String iconUrl;
        private String id;
        private String name;
        private String self;
        private StatusCategory statusCategory;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusCategory {

        private String colorName;
        private int id;
        private String key;
        private String name;
        private String self;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Votes {

        private boolean hasVoted;
        private String self;
        private int votes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Watches {

        private boolean isWatching;
        private String self;
        private int watchCount;
    }

    /**
     * Top-level object that represents a Jira comment.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comment {

        private Author author;
        private String body;
        private String created;
        private String id;

        @JsonProperty("jsdPublic")
        private Boolean jsdPublic;

        private String self;

        @JsonProperty("updateAuthor")
        private Author updateAuthor;

        private String updated;

        // ── getters & setters ──────────────────────────────────────────────────────
        public Author getAuthor()             { return author; }
        public void   setAuthor(Author author){ this.author = author; }

        public String getBody()               { return body; }
        public void   setBody(String body)    { this.body = body; }

        public String getCreated()            { return created; }
        public void   setCreated(String created) { this.created = created; }

        public String getId()                 { return id; }
        public void   setId(String id)        { this.id = id; }

        public Boolean getJsdPublic()         { return jsdPublic; }
        public void    setJsdPublic(Boolean jsdPublic){ this.jsdPublic = jsdPublic; }

        public String getSelf()               { return self; }
        public void   setSelf(String self)    { this.self = self; }

        public Author getUpdateAuthor()       { return updateAuthor; }
        public void   setUpdateAuthor(Author updateAuthor){ this.updateAuthor = updateAuthor; }

        public String getUpdated()            { return updated; }
        public void   setUpdated(String updated){ this.updated = updated; }

        @Override
        public String toString() {
            return "Comment{" +
                "author=" + author +
                ", body='" + body + '\'' +
                ", created='" + created + '\'' +
                ", id='" + id + '\'' +
                ", jsdPublic=" + jsdPublic +
                ", self='" + self + '\'' +
                ", updateAuthor=" + updateAuthor +
                ", updated='" + updated + '\'' +
                '}';
        }
    }

    /**
     * Re-usable structure for both "author" and "updateAuthor".
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Author {

        private String accountId;
        private String accountType;
        private boolean active;

        @JsonProperty("avatarUrls")
        private AvatarUrls avatarUrls;

        private String displayName;
        private String self;
        private String timeZone;

        // ── getters & setters ──────────────────────────────────────────────────────
        public String getAccountId()              { return accountId; }
        public void   setAccountId(String accountId){ this.accountId = accountId; }

        public String getAccountType()            { return accountType; }
        public void   setAccountType(String accountType){ this.accountType = accountType; }

        public boolean isActive()                 { return active; }
        public void    setActive(boolean active)  { this.active = active; }

        public AvatarUrls getAvatarUrls()         { return avatarUrls; }
        public void      setAvatarUrls(AvatarUrls avatarUrls){ this.avatarUrls = avatarUrls; }

        public String getDisplayName()            { return displayName; }
        public void   setDisplayName(String displayName){ this.displayName = displayName; }

        public String getSelf()                   { return self; }
        public void   setSelf(String self)        { this.self = self; }

        public String getTimeZone()               { return timeZone; }
        public void   setTimeZone(String timeZone){ this.timeZone = timeZone; }

        @Override
        public String toString() {
            return "Author{" +
                "accountId='" + accountId + '\'' +
                ", accountType='" + accountType + '\'' +
                ", active=" + active +
                ", avatarUrls=" + avatarUrls +
                ", displayName='" + displayName + '\'' +
                ", self='" + self + '\'' +
                ", timeZone='" + timeZone + '\'' +
                '}';
        }
    }

    /**
     * Holds the different avatar sizes.
     * Property names starting with digits are mapped via @JsonProperty.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    class AvatarUrls {

        @JsonProperty("16x16")
        private String size16x16;

        @JsonProperty("24x24")
        private String size24x24;

        @JsonProperty("32x32")
        private String size32x32;

        @JsonProperty("48x48")
        private String size48x48;

        // ── getters & setters ──────────────────────────────────────────────────────
        public String getSize16x16()              { return size16x16; }
        public void   setSize16x16(String url)    { this.size16x16 = url; }

        public String getSize24x24()              { return size24x24; }
        public void   setSize24x24(String url)    { this.size24x24 = url; }

        public String getSize32x32()              { return size32x32; }
        public void   setSize32x32(String url)    { this.size32x32 = url; }

        public String getSize48x48()              { return size48x48; }
        public void   setSize48x48(String url)    { this.size48x48 = url; }

        @Override
        public String toString() {
            return "AvatarUrls{" +
                "size16x16='" + size16x16 + '\'' +
                ", size24x24='" + size24x24 + '\'' +
                ", size32x32='" + size32x32 + '\'' +
                ", size48x48='" + size48x48 + '\'' +
                '}';
        }
    }

}