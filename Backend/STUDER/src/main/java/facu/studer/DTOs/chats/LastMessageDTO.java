package facu.studer.DTOs.chats;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LastMessageDTO {
    private String content;
    private LocalDateTime timestamp;
    private Boolean isRead;
}
