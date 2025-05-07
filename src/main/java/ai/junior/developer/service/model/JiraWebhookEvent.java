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
}