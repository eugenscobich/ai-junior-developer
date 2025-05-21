package ai.junior.developer.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
public class GitHubCreatePullRequestResponse {

    private String url;
    private long id;

    @JsonProperty("node_id")
    private String nodeId;
    @JsonProperty("html_url")
    private String htmlUrl;
    @JsonProperty("diff_url")
    private String diffUrl;
    @JsonProperty("patch_url")
    private String patchUrl;

    @JsonProperty("issue_url")
    private String issueUrl;
    @JsonProperty("commits_url")
    private String commitsUrl;
    @JsonProperty("review_comments_url")
    private String reviewCommentsUrl;
    @JsonProperty("review_comment_url")
    private String reviewCommentUrl;
    @JsonProperty("comments_url")
    private String commentsUrl;
    @JsonProperty("statuses_url")
    private String statusesUrl;

    private int number;
    private String state;
    private boolean locked;
    private String title;

    private User user;
    private String body;
    private List<Label> labels;
    private Milestone milestone;

    @JsonProperty("active_lock_reason")
    private String activeLockReason;
    @JsonProperty("created_at")
    private Instant createdAt;
    @JsonProperty("updated_at")
    private Instant updatedAt;
    @JsonProperty("closed_at")
    private Instant closedAt;
    @JsonProperty("merged_at")
    private Instant mergedAt;

    @JsonProperty("merge_commit_sha")
    private String mergeCommitSha;

    private User assignee;
    private List<User> assignees;
    @JsonProperty("requested_reviewers")
    private List<User> requestedReviewers;
    @JsonProperty("requested_teams")
    private List<Team> requestedTeams;

    private Head head;
    private Base base;

    @JsonProperty("_links")
    private Links links;

    @JsonProperty("author_association")
    private String authorAssociation;
    @JsonProperty("auto_merge")
    private AutoMerge autoMerge;

    private boolean draft;
    private boolean merged;
    private Boolean mergeable;
    private Boolean rebaseable;
    @JsonProperty("mergeable_state")
    private String mergeableState;

    @JsonProperty("merged_by")
    private User mergedBy;

    private int comments;
    @JsonProperty("review_comments")
    private int reviewComments;
    @JsonProperty("maintainer_can_modify")
    private boolean maintainerCanModify;
    private int commits;
    private int additions;
    private int deletions;
    @JsonProperty("changed_files")
    private int changedFiles;

    /* ----------  Nested types ---------- */

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {

        private String login;
        private long id;
        @JsonProperty("node_id")
        private String nodeId;
        @JsonProperty("avatar_url")
        private String avatarUrl;
        @JsonProperty("gravatar_id")
        private String gravatarId;
        private String url;
        @JsonProperty("html_url")
        private String htmlUrl;
        @JsonProperty("followers_url")
        private String followersUrl;
        @JsonProperty("following_url")
        private String followingUrl;
        @JsonProperty("gists_url")
        private String gistsUrl;
        @JsonProperty("starred_url")
        private String starredUrl;
        @JsonProperty("subscriptions_url")
        private String subscriptionsUrl;
        @JsonProperty("organizations_url")
        private String organizationsUrl;
        @JsonProperty("repos_url")
        private String reposUrl;
        @JsonProperty("events_url")
        private String eventsUrl;
        @JsonProperty("received_events_url")
        private String receivedEventsUrl;
        private String type;
        @JsonProperty("site_admin")
        private boolean siteAdmin;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Label {

        private long id;
        @JsonProperty("node_id")
        private String nodeId;
        private String url;
        private String name;
        private String description;
        private String color;
        @JsonProperty("default")
        private boolean isDefault;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Milestone {

        private String url;
        @JsonProperty("html_url")
        private String htmlUrl;
        @JsonProperty("labels_url")
        private String labelsUrl;
        private long id;
        @JsonProperty("node_id")
        private String nodeId;
        private int number;
        private String state;
        private String title;
        private String description;
        private User creator;
        @JsonProperty("open_issues")
        private int openIssues;
        @JsonProperty("closed_issues")
        private int closedIssues;
        @JsonProperty("created_at")
        private Instant createdAt;
        @JsonProperty("updated_at")
        private Instant updatedAt;
        @JsonProperty("closed_at")
        private Instant closedAt;
        @JsonProperty("due_on")
        private Instant dueOn;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Team {

        private long id;
        @JsonProperty("node_id")
        private String nodeId;
        private String url;
        @JsonProperty("html_url")
        private String htmlUrl;
        private String name;
        private String slug;
        private String description;
        private String privacy;
        @JsonProperty("notification_setting")
        private String notificationSetting;
        private String permission;
        @JsonProperty("members_url")
        private String membersUrl;
        @JsonProperty("repositories_url")
        private String repositoriesUrl;
    }

    /* head & base share the same structure */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Head {

        private String label;
        private String ref;
        private String sha;
        private User user;
        private Repo repo;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Base {

        private String label;
        private String ref;
        private String sha;
        private User user;
        private Repo repo;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repo {

        private long id;
        @JsonProperty("node_id")
        private String nodeId;
        private String name;
        @JsonProperty("full_name")
        private String fullName;
        private User owner;
        private boolean _private; // “private” is a keyword
        @JsonProperty("html_url")
        private String htmlUrl;
        private String description;
        private boolean fork;
        private String url;

        /* lots of additional URLs & metadata … */
        @JsonProperty("clone_url")
        private String cloneUrl;
        @JsonProperty("git_url")
        private String gitUrl;
        @JsonProperty("ssh_url")
        private String sshUrl;
        @JsonProperty("svn_url")
        private String svnUrl;
        private String homepage;
        @JsonProperty("forks_count")
        private int forksCount;
        @JsonProperty("stargazers_count")
        private int stargazersCount;
        @JsonProperty("watchers_count")
        private int watchersCount;
        private int size;
        @JsonProperty("default_branch")
        private String defaultBranch;
        @JsonProperty("open_issues_count")
        private int openIssuesCount;
        private List<String> topics;

        @JsonProperty("has_issues")
        private boolean hasIssues;
        @JsonProperty("has_projects")
        private boolean hasProjects;
        @JsonProperty("has_wiki")
        private boolean hasWiki;
        @JsonProperty("has_pages")
        private boolean hasPages;
        @JsonProperty("has_downloads")
        private boolean hasDownloads;
        @JsonProperty("has_discussions")
        private boolean hasDiscussions;
        private boolean archived;
        private boolean disabled;
        @JsonProperty("pushed_at")
        private Instant pushedAt;
        @JsonProperty("created_at")
        private Instant createdAt;
        @JsonProperty("updated_at")
        private Instant updatedAt;

        private Permissions permissions;
        private License license;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Permissions {

        private boolean admin;
        private boolean push;
        private boolean pull;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class License {

        private String key;
        private String name;
        private String spdxId;
        private String url;
        @JsonProperty("node_id")
        private String nodeId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {

        private Link self;
        private Link html;
        private Link issue;
        private Link comments;
        @JsonProperty("review_comments")
        private Link reviewComments;
        @JsonProperty("review_comment")
        private Link reviewComment;
        private Link commits;
        private Link statuses;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Link {

        private String href;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AutoMerge {

        // The GitHub API returns null today; reserve the type in case it gains structure.
        private Map<String, Object> raw;
    }
}
