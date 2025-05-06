package ai.junior.developer.service.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GitHubCreatePullRequestPayload {

    String title;
    String body;
    String head;
    String base;
}
