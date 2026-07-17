package facu.studer.services.implementation;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.feed.PostCreateRequestDTO;
import facu.studer.DTOs.feed.PostPageResponseDTO;
import facu.studer.DTOs.feed.PostResponseDTO;
import facu.studer.entities.Tag;
import facu.studer.entities.feed.Post;
import facu.studer.entities.feed.PostLike;
import facu.studer.entities.users.Friend;
import facu.studer.entities.users.User;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.mappers.PostMapper;
import facu.studer.repositories.FriendRepository;
import facu.studer.repositories.feed.PostLikeRepository;
import facu.studer.repositories.feed.PostRepository;
import facu.studer.services.FeedService;
import facu.studer.services.PointsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedServiceImpl implements FeedService {

    private static final int PAGE_SIZE = 15;

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final FriendRepository friendRepository;
    private final PointsService pointsService;

    @PersistenceContext
    private EntityManager entityManager;

    public FeedServiceImpl(PostRepository postRepository,
                           PostLikeRepository postLikeRepository,
                           FriendRepository friendRepository,
                           PointsService pointsService) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.friendRepository = friendRepository;
        this.pointsService = pointsService;
    }

    @Override
    @Transactional
    public PostResponseDTO createPost(String username, PostCreateRequestDTO request) {
        User user = findUserByUsername(username);
        Set<Tag> managedTags = resolveTagsByName(request.getTags());

        Post post = Post.builder()
                .user(user)
                .content(request.getContent())
                .tags(managedTags)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        Post saved = postRepository.save(post);
        entityManager.flush();

        return PostMapper.toResponseDTO(saved, 0, false);
    }

    @Override
    @Transactional(readOnly = true)
    public PostPageResponseDTO getFeed(String username, int page, String filter) {
        User user = findUserByUsername(username);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Post> postPage;

        if ("following".equalsIgnoreCase(filter)) {
            List<Friend> friendships = friendRepository.findConfirmedFriends(user);
            Set<Long> friendIds = friendships.stream()
                    .map(f -> f.getSender().getId().equals(user.getId()) ? f.getReceiver().getId() : f.getSender().getId())
                    .collect(Collectors.toSet());
            friendIds.add(user.getId());
            postPage = postRepository.findByUserIds(new ArrayList<>(friendIds), pageable);
        } else if ("popular".equalsIgnoreCase(filter)) {
            postPage = postRepository.findPopularPosts(pageable);
        } else {
            postPage = postRepository.findRecentPosts(pageable);
        }

        List<PostResponseDTO> dtos = postPage.getContent().stream()
                .map(p -> mapToDTO(p, username))
                .collect(Collectors.toList());

        return PostPageResponseDTO.builder()
                .posts(dtos)
                .totalElements(postPage.getTotalElements())
                .hasMore(postPage.hasNext())
                .currentPage(page)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PostPageResponseDTO getUserPosts(String username, int page, Long userId) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Post> postPage = postRepository.findByUserId(userId, pageable);

        List<PostResponseDTO> dtos = postPage.getContent().stream()
                .map(p -> mapToDTO(p, username))
                .collect(Collectors.toList());

        return PostPageResponseDTO.builder()
                .posts(dtos)
                .totalElements(postPage.getTotalElements())
                .hasMore(postPage.hasNext())
                .currentPage(page)
                .build();
    }

    @Override
    @Transactional
    public MessageDTO likePost(String username, Long postId) {
        if (postLikeRepository.existsByUserUsernameAndPostIdAndIsActiveTrue(username, postId)) {
            throw new IllegalArgumentException("post.already_liked");
        }

        User user = findUserByUsername(username);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post.not_found"));

        PostLike like = PostLike.builder()
                .user(user)
                .post(post)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        postLikeRepository.save(like);

        pointsService.addPoints(post.getUser(), 1L);

        return MessageDTO.builder()
                .success(true)
                .message("post.like_added")
                .build();
    }

    @Override
    @Transactional
    public MessageDTO unlikePost(String username, Long postId) {
        var likeOpt = postLikeRepository.findByUserUsernameAndPostIdAndIsActiveTrue(username, postId);

        if (likeOpt.isEmpty()) {
            throw new IllegalArgumentException("post.not_liked");
        }

        PostLike like = likeOpt.get();
        User postOwner = like.getPost().getUser();

        like.setIsActive(false);
        like.setLastUpdatedDatetime(LocalDateTime.now());
        postLikeRepository.save(like);

        pointsService.deductPoints(postOwner, 1L);

        return MessageDTO.builder()
                .success(true)
                .message("post.like_removed")
                .build();
    }

    private PostResponseDTO mapToDTO(Post post, String username) {
        long likeCount = postLikeRepository.countByPostId(post.getId());
        boolean likedByUser = postLikeRepository.isLikedByUser(post.getId(), username);
        return PostMapper.toResponseDTO(post, likeCount, likedByUser);
    }

    private User findUserByUsername(String username) {
        var query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.username = :username AND u.isActive = true", User.class);
        query.setParameter("username", username);
        var results = query.getResultList();
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("user.not_found");
        }
        return results.get(0);
    }

    private Set<Tag> resolveTagsByName(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> normalized = tagNames.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(n -> !n.isBlank())
                .collect(Collectors.toSet());

        if (normalized.isEmpty()) {
            return new HashSet<>();
        }

        List<Tag> existing = entityManager.createQuery(
                        "SELECT t FROM Tag t WHERE t.name IN :names AND t.isActive = true", Tag.class)
                .setParameter("names", normalized)
                .getResultList();

        Set<String> existingNames = existing.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        Set<Tag> result = new HashSet<>(existing);

        for (String name : normalized) {
            if (!existingNames.contains(name)) {
                Tag newTag = Tag.builder()
                        .name(name)
                        .isActive(true)
                        .createdDatetime(LocalDateTime.now())
                        .lastUpdatedDatetime(LocalDateTime.now())
                        .build();
                entityManager.persist(newTag);
                result.add(newTag);
            }
        }

        return result;
    }
}
