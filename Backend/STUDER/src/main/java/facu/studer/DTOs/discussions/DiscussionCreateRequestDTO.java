package facu.studer.DTOs.discussions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/**
 * DTO for creating a new public discussion.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DiscussionCreateRequestDTO {

    /** Title of the discussion. */
    @NotBlank(message = "discussion.title.required")
    @Size(min = 3, max = 200, message = "discussion.title.size")
    private String title;

    /** Content of the first message / description. */
    @NotBlank(message = "discussion.description.required")
    private String description;

    /** Optional list of tag names to associate. */
    private List<String> tags;

    /** Optional image reference for the first message. */
    @JsonProperty("image_ref")
    private String imageRef;
}

