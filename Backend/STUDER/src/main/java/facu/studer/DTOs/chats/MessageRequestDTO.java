package facu.studer.DTOs.chats;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MessageRequestDTO {

    @NotBlank(message = "Content cannot be empty")
    private String content;

    private Long replyToId;
}
