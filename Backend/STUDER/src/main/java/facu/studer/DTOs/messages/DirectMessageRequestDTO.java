package facu.studer.DTOs.messages;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sending a direct message.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DirectMessageRequestDTO {

    /**
     * ID of the user receiving the message.
     */
    @NotNull(message = "message.receiver.required")
    @JsonAlias({"receiverId"})
    private Long receiverId;

    /**
     * Content of the message.
     */
    @NotBlank(message = "message.content.required")
    private String content;

    /**
     * Optional link to media/attachment.
     */
    private String link;

    /**
     * ID of the message being replied to (optional).
     */
    @JsonAlias({"replyToId"})
    private Long replyToId;
}

