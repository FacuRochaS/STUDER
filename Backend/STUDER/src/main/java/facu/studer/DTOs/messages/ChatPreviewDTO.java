package facu.studer.DTOs.messages;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for chat preview in chat list.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatPreviewDTO {

    /**
     * Other user's ID.
     */
    private Long userId;

    /**
     * Other user's username.
     */
    private String username;

    /**
     * Other user's first name.
     */
    private String firstName;

    /**
     * Other user's last name.
     */
    private String lastName;

    /**
     * Last message content.
     */
    private String lastMessageContent;

    /**
     * Last message sent time.
     */
    private LocalDateTime lastMessageTime;

    /**
     * Number of unread messages from this user.
     */
    private Integer unreadCount;

    /**
     * Whether the last message is from the current user.
     */
    private Boolean lastMessageIsFromMe;
}

