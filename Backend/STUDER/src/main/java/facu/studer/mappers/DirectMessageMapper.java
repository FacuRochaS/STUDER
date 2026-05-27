package facu.studer.mappers;

import facu.studer.DTOs.messages.DirectMessageResponseDTO;
import facu.studer.entities.messages.DirectMessage;

/**
 * Mapper for DirectMessage entity and related DTOs.
 */
public final class DirectMessageMapper {
    private DirectMessageMapper() { }

    /**
     * Maps a DirectMessage entity to a DirectMessageResponseDTO.
     * @param message the DirectMessage entity
     * @return the response DTO
     */
    public static DirectMessageResponseDTO toResponseDTO(DirectMessage message) {
        if (message == null) {
            return null;
        }
        return DirectMessageResponseDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .receiverId(message.getReceiver().getId())
                .receiverUsername(message.getReceiver().getUsername())
                .content(message.getContent())
                .link(message.getLink())
                .sentAt(message.getSentAt())
                .isRead(message.getIsRead())
                .replyToId(message.getReplyTo() != null ? message.getReplyTo().getId() : null)
                .replyToContent(message.getReplyTo() != null ? message.getReplyTo().getContent() : null)
                .build();
    }
}

