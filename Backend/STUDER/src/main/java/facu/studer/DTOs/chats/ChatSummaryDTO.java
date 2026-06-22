package facu.studer.DTOs.chats;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import facu.studer.DTOs.friends.FriendStatusResponseDTO;
import facu.studer.DTOs.user.UserPublicResponseDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatSummaryDTO {
    private Long chatId;
    private UserPublicResponseDTO otherUser;
    private FriendStatusResponseDTO friendStatus;
    private LastMessageDTO lastMessage;
}
