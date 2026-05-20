package facu.studer.DTOs.discussions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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

    @JsonProperty("owner_username")
    private String ownerUsername;

    private List<String> tags;
    private boolean closed;

    @JsonProperty("closed_at")
    private LocalDateTime closedAt;

    @JsonProperty("message_count")
    private int messageCount;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    private boolean favourite;

    @JsonProperty("participation_type")
    private String participationType;
}

