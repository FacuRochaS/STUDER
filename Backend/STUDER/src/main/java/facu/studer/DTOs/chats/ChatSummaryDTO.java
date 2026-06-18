package facu.studer.DTOs.chats;

import facu.studer.DTOs.friends.FriendStatusResponseDTO;
import facu.studer.DTOs.user.UserPublicResponseDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatSummaryDTO {
    private Long chatId;
    private UserPublicResponseDTO otherUser;
    private FriendStatusResponseDTO friendStatus;
    private LastMessageDTO lastMessage;
}
