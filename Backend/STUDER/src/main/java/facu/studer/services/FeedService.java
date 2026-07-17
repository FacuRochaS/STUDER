package facu.studer.services;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.feed.PostCreateRequestDTO;
import facu.studer.DTOs.feed.PostPageResponseDTO;
import facu.studer.DTOs.feed.PostResponseDTO;

public interface FeedService {
    PostResponseDTO createPost(String username, PostCreateRequestDTO request);
    PostPageResponseDTO getFeed(String username, int page, String filter);
    PostPageResponseDTO getUserPosts(String username, int page, Long userId);
    MessageDTO likePost(String username, Long postId);
    MessageDTO unlikePost(String username, Long postId);
}
