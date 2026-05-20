package facu.studer.DTOs.discussions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for a discussion message response.
 * Includes like information and nested children for hierarchy.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DiscussionMessageResponseDTO {

    private Long id;

    @JsonProperty("sender_username")
    private String senderUsername;

    private String content;

    @JsonProperty("image_ref")
    private String imageRef;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("like_count")
    private long likeCount;

    @JsonProperty("liked_by_current_user")
    private boolean likedByCurrentUser;

    private List<DiscussionMessageResponseDTO> children;
}

