package facu.studer.DTOs.chats;

import lombok.Data;

@Data
public class MessageRequestDTO {
    private String content;
    private String link;
    private Long replyToId;
}
