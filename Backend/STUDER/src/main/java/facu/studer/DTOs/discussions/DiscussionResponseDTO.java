package facu.studer.DTOs.discussions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import facu.studer.DTOs.user.UserPublicResponseDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for a discussion response.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DiscussionResponseDTO {

    private Long id;

    private String title;

    private String description;

    private UserPublicResponseDTO owner;

    private List<String> tags;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    private boolean favourite;

    @JsonProperty("participation_type")
    private String participationType;

    @JsonProperty("message_count")
    private int messageCount;

    @JsonProperty("like_count")
    private int likeCount;

    @JsonProperty("favourite_count")
    private int favouriteCount;




}

