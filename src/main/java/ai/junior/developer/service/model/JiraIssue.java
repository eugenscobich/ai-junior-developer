package ai.junior.developer.service.model;

import ai.junior.developer.service.model.JiraCommentsResponse.Body;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Top-level Jira issue.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssue {

    private String expand;
    private Fields fields;
    private String id;
    private String key;
    private String self;

    /**
     * “fields” section.
     * Only the attributes that appear in your sample are mapped.
     * Everything unknown (including custom fields like customfield_10059) is captured in {@code extras}.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fields {

        private Aggregate aggregateprogress;
        private Integer aggregatetimeestimate;
        private Integer aggregatetimeoriginalestimate;
        private Integer aggregatetimespent;

        private User assignee;
        private List<Attachment> attachment;
        private CommentContainer comment;
        private List<Component> components;
        private String created;
        private User creator;

        private Body description;          // Change to a richer type if you need structured content
        private String duedate;

        private IssueType issuetype;
        private List<String> labels;
        private String lastViewed;
        private Priority priority;
        private Aggregate progress;
        private Project project;
        private User reporter;
        private Status status;
        private StatusCategory statusCategory;
        private String statuscategorychangedate;

        private String summary;
        private Integer timeestimate;
        private Integer timeoriginalestimate;
        private Integer timespent;
        private Timetracking timetracking;
        private String updated;
        private List<Version> versions;
        private Votes votes;
        private Watches watches;
        private Worklog worklog;
        private Integer workratio;

        // Any custom or unknown Jira fields end up here
        private final Map<String, Object> extras = new HashMap<>();
        @JsonAnySetter
        public void addExtra(String key, Object value) {
            extras.put(key, value);
        }
    }

    /* ---------- Leaf / helper classes ---------- */

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Aggregate {
        private int progress;
        private int total;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String accountId;
        private String accountType;
        private boolean active;
        private AvatarUrls avatarUrls;
        private String displayName;
        private String emailAddress;
        private String self;
        private String timeZone;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AvatarUrls {
        private String _16x16;
        private String _24x24;
        private String _32x32;
        private String _48x48;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommentContainer {
        private List<Comment> comments;
        private int maxResults;
        private String self;
        private int startAt;
        private int total;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comment {
        // add body, author, etc. when needed
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Component {
        // id, name, description, …
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueType {
        private String id;
        private String name;
        private String description;
        private boolean subtask;
        private String iconUrl;
        private int hierarchyLevel;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Priority {
        private String id;
        private String name;
        private String iconUrl;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Project {
        private String id;
        private String key;
        private String name;
        private String projectTypeKey;
        private boolean simplified;
        private AvatarUrls avatarUrls;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private String id;
        private String name;
        private String description;
        private String iconUrl;
        private StatusCategory statusCategory;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusCategory {
        private int id;
        private String key;
        private String name;
        private String colorName;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Timetracking {
        // originalEstimate, remainingEstimate, …
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Version {
        // id, name, released, …
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

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Worklog {
        private int maxResults;
        private int startAt;
        private int total;
        private List<Object> worklogs;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachment {
        // id, filename, content, size, …
    }
}
