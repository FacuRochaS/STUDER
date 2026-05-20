package facu.studer.mappers;

import facu.studer.DTOs.discussions.DiscussionMessageResponseDTO;
import facu.studer.entities.discussions.DiscussionMessage;

import java.util.List;

/**
 * Mapper for DiscussionMessage entities to DTOs.
 */
public final class DiscussionMessageMapper {

    private DiscussionMessageMapper() { }

    /**
     * Maps a DiscussionMessage entity to a DiscussionMessageResponseDTO.
     *
     * @param message             the message entity
     * @param likeCount           total likes on the message
     * @param likedByCurrentUser  whether the current user liked it
     * @param children            pre-mapped child DTOs
     * @return the response DTO
     */
    public static DiscussionMessageResponseDTO toResponseDTO(
            DiscussionMessage message,
            long likeCount,
            boolean likedByCurrentUser,
            List<DiscussionMessageResponseDTO> children) {

        if (message == null) {
            return null;
        }

        return DiscussionMessageResponseDTO.builder()
                .id(message.getId())
                .senderUsername(message.getSender().getUsername())
                .content(message.getContent())
                .imageRef(message.getImageRef())
                .createdAt(message.getCreatedDatetime())
                .likeCount(likeCount)
                .likedByCurrentUser(likedByCurrentUser)
                .children(children != null ? children : List.of())
                .build();
    }
}

