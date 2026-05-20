package facu.studer.DTOs.discussions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for creating a new message in a discussion (or replying to one).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DiscussionMessageCreateRequestDTO {

    /** Content of the message. */
    @NotBlank(message = "discussion.message.content.required")
    private String content;

    /** ID of the parent message (null for root-level messages). */
    @JsonProperty("parent_message_id")
    private Long parentMessageId;

    /** Optional image reference (stored in external microservice). */
    @JsonProperty("image_ref")
    private String imageRef;
}

