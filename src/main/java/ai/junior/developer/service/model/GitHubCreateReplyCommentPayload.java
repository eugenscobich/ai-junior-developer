package ai.junior.developer.service.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GitHubCreateReplyCommentPayload {

    String body;

}
