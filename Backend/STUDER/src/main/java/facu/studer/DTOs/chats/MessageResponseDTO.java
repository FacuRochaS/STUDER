package facu.studer.DTOs.chats;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.time.LocalDateTime;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MessageResponseDTO {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String content;
    private String link;
    private Long replyToId;
    private Boolean isRead;
    private LocalDateTime createdDatetime;
}
