package facu.studer.mappers;

import facu.studer.DTOs.user.FriendResponseDTO;
import facu.studer.entities.Friend;
import facu.studer.entities.User;

/**
 * Mapper for Friend entity and related DTOs.
 */
public final class FriendMapper {
    private FriendMapper() { }

    /**
     * Maps a Friend entity to a FriendResponseDTO, with the other user's info.
     * @param friend the Friend entity
     * @param currentUserId the current user's ID (to determine which user is the "friend")
     * @return the response DTO
     */
    public static FriendResponseDTO toResponseDTO(Friend friend, Long currentUserId) {
        if (friend == null) {
            return null;
        }

        // Determine which user is the friend (the other one)
        User friendUser = friend.getSender().getId().equals(currentUserId) 
            ? friend.getReceiver() 
            : friend.getSender();

        boolean isFriend = friend.getSenderAccept() && friend.getReceiverAccept();

        return FriendResponseDTO.builder()
                .id(friend.getId())
                .userId(friendUser.getId())
                .username(friendUser.getUsername())
                .firstName(friendUser.getFirstName())
                .lastName(friendUser.getLastName())
                .email(friendUser.getEmail())
                .isFriend(isFriend)
                .build();
    }
}

