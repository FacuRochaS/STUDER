package facu.studer.mappers;

import facu.studer.DTOs.discussions.DiscussionResponseDTO;
import facu.studer.entities.Tag;
import facu.studer.entities.discussions.Discussion;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Discussion entities to DTOs.
 */
public final class DiscussionMapper {

    private DiscussionMapper() { }

    /**
     * Maps a Discussion entity to a DiscussionResponseDTO.
     *
     * @param discussion        the discussion entity
     * @param isFavourite       whether the current user has favourited it
     * @param participationType the current user's participation type (OWNER, MESSAGED, FAVOURITE, NONE)
     * @return the response DTO
     */
    public static DiscussionResponseDTO toResponseDTO(
            Discussion discussion,
            boolean isFavourite,
            String participationType) {

        if (discussion == null) {
            return null;
        }

        List<String> tagNames = discussion.getTags() != null
                ? discussion.getTags().stream().map(Tag::getName).collect(Collectors.toList())
                : List.of();

        return DiscussionResponseDTO.builder()
                .id(discussion.getId())
                .title(discussion.getTitle())
                .description(discussion.getDescription())
                .ownerUsername(discussion.getOwner().getUsername())
                .tags(tagNames)
                .closed(discussion.isClosed())
                .closedAt(discussion.getClosedAt())
                .messageCount(discussion.getMessageCount())
                .createdAt(discussion.getCreatedDatetime())
                .favourite(isFavourite)
                .participationType(participationType)
                .build();
    }
}

