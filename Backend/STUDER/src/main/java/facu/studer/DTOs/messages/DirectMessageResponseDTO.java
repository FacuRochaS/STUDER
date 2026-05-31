package facu.studer.DTOs.messages;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for direct message response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DirectMessageResponseDTO {

    /**
     * Message ID.
     */
    private Long id;

    /**
     * Sender user ID.
     */
    private Long senderId;

    /**
     * Sender username.
     */
    private String senderUsername;

    /**
     * Receiver user ID.
     */
    private Long receiverId;

    /**
     * Receiver username.
     */
    private String receiverUsername;

    /**
     * Message content.
     */
    private String content;

    /**
     * Optional attachment link.
     */
    private String link;

    /**
     * Timestamp when sent.
     */
    private LocalDateTime sentAt;

    /**
     * Whether the message has been read.
     */
    private Boolean isRead;

    /**
     * ID of the message being replied to (if applicable).
     */
    private Long replyToId;

    /**
     * Content of the replied message (if applicable).
     */
    private String replyToContent;
}

