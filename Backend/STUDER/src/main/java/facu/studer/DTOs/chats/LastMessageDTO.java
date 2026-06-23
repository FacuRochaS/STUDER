package facu.studer.DTOs.chats;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LastMessageDTO {
    private Long senderId;
    private String content;
    private LocalDateTime timestamp;
    private Boolean isRead;
}
