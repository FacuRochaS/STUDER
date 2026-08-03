package facu.studer.mappers;

import facu.studer.DTOs.feed.PostResponseDTO;
import facu.studer.entities.Tag;
import facu.studer.entities.feed.Post;

import java.util.List;
import java.util.stream.Collectors;

public final class PostMapper {
    private PostMapper() {}

    public static PostResponseDTO toResponseDTO(Post post, long likeCount, boolean likedByCurrentUser) {
        if (post == null) return null;

        List<String> tagNames = post.getTags() != null
                ? post.getTags().stream().map(Tag::getName).collect(Collectors.toList())
                : List.of();

        return PostResponseDTO.builder()
                .id(post.getId())
                .user(UserMapper.toPublicSimpleResponseDTO(post.getUser()))
                .content(post.getContent() != null ? post.getContent().toString() : null)
                .tags(tagNames)
                .createdDatetime(post.getCreatedDatetime())
                .likeCount(likeCount)
                .likedByCurrentUser(likedByCurrentUser)
                .build();
    }
}
