package facu.studer.services.implementation;

import facu.studer.DTOs.discussions.*;
import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.media.ImageUploadResponseDTO;
import facu.studer.entities.BaseEntity;
import facu.studer.entities.Tag;
import facu.studer.entities.users.User;
import facu.studer.entities.discussions.Discussion;
import facu.studer.entities.discussions.DiscussionMessage;
import facu.studer.entities.discussions.MessageLike;
import facu.studer.entities.discussions.UserDiscussionFav;
import facu.studer.exceptions.DiscussionClosedException;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.exceptions.UnauthorizedOperationException;
import facu.studer.mappers.DiscussionMapper;
import facu.studer.mappers.DiscussionMessageMapper;
import facu.studer.repositories.discussion.DiscussionMessageRepository;
import facu.studer.repositories.discussion.DiscussionRepository;
import facu.studer.repositories.discussion.MessageLikeRepository;
import facu.studer.repositories.discussion.UserDiscussionFavRepository;
import facu.studer.services.DiscussionService;
import facu.studer.services.PointsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Order.by;

/**
 * Implementation of DiscussionService.
 * Uses only DiscussionRepository (respects 1-repo-per-service).
 * Uses EntityManager for User lookups and favourite checks.
 */
@Service
public class DiscussionServiceImpl implements DiscussionService {

    private static final int PAGE_SIZE = 15;
    private static final int DEFAULT_ACTIVITY_HOURS = 24;

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final RestTemplate restTemplate;
    private final String mediaServiceUrl;

    private final DiscussionRepository discussionRepository;
    private final MessageLikeRepository messageLikeRepository;
    private final DiscussionMessageRepository discussionMessageRepository;
    private final UserDiscussionFavRepository userDiscussionFavRepository;
    private final PointsService pointsService;

    @PersistenceContext
    private EntityManager entityManager;

    public DiscussionServiceImpl(DiscussionRepository discussionRepository,
                                 MessageLikeRepository messageLikeRepository,
                                 DiscussionMessageRepository discussionMessageRepository,
                                 UserDiscussionFavRepository userDiscussionFavRepository,
                                 RestTemplate restTemplate,
                                 @Value("${app.media.service.url}") String mediaServiceUrl,
                                 PointsService pointsService) {
        this.discussionRepository = discussionRepository;
        this.messageLikeRepository = messageLikeRepository;
        this.discussionMessageRepository = discussionMessageRepository;
        this.userDiscussionFavRepository = userDiscussionFavRepository;
        this.restTemplate = restTemplate;
        this.mediaServiceUrl = mediaServiceUrl;
        this.pointsService = pointsService;
    }

    /**
     * Gets paginated public discussions with filters, ordered by recent activity.
     */
    @Override
    @Transactional(readOnly = true)
    public DiscussionPageResponseDTO getPublicDiscussions(
            String username,
            int page,
            List<String> tagNames,
            Integer lastDays,
            Integer activityHours) {

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        LocalDateTime since = lastDays != null ? LocalDateTime.now().minusDays(lastDays) : null;
        int hours = activityHours != null ? activityHours : DEFAULT_ACTIVITY_HOURS;
        LocalDateTime activitySince = LocalDateTime.now().minusHours(hours);
        boolean hasTags = tagNames != null && !tagNames.isEmpty();

        Page<Discussion> discussionPage = discussionRepository.findPublicDiscussionsByActivity(
                hasTags,
                hasTags ? tagNames : List.of(),
                since,
                activitySince,
                pageable);

        List<DiscussionResponseDTO> dtos = discussionPage.getContent().stream()
                .map(d -> mapToDTO(d, username))
                .collect(Collectors.toList());

        return DiscussionPageResponseDTO.builder()
                .discussions(dtos)
                .totalElements(discussionPage.getTotalElements())
                .hasMore(discussionPage.hasNext())
                .currentPage(page)
                .build();
    }

    /**
     * Gets discussions where the user participates (owner or messaged) and favourited.
     */
    @Override
    @Transactional(readOnly = true)
    public DiscussionPageResponseDTO getUserDiscussions(String username, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        Page<Discussion> discussionPage = discussionRepository
                .findByUserParticipation(username, pageable);

        List<DiscussionResponseDTO> dtos = discussionPage.getContent().stream()
                .map(d -> mapToDTO(d, username))
                .collect(Collectors.toList());

        return DiscussionPageResponseDTO.builder()
                .discussions(dtos)
                .totalElements(discussionPage.getTotalElements())
                .hasMore(discussionPage.hasNext())
                .currentPage(page)
                .build();
    }




    @Override
    public DiscussionPageResponseDTO getUserOwnDiscussions(String username, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        Page<Discussion> discussionPage = discussionRepository
                .findByUsername(username, pageable);

        List<DiscussionResponseDTO> dtos = discussionPage.getContent().stream()
                .map(d -> mapToDTO(d, username))
                .collect(Collectors.toList());

        return DiscussionPageResponseDTO.builder()
                .discussions(dtos)
                .totalElements(discussionPage.getTotalElements())
                .hasMore(discussionPage.hasNext())
                .currentPage(page)
                .build();
    }

    @Override
    public DiscussionPageResponseDTO getNewDiscussions(String username, int page) {
        Sort sort = Sort.sort(Discussion.class).by(Discussion::getCreatedDatetime).descending();

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, sort);

        Page<Discussion> discussionPage = discussionRepository.findAll(pageable);

        List<DiscussionResponseDTO> dtos = discussionPage.getContent().stream()
                .map(d -> mapToDTO(d, username))
                .collect(Collectors.toList());

        return DiscussionPageResponseDTO.builder()
                .discussions(dtos)
                .totalElements(discussionPage.getTotalElements())
                .hasMore(discussionPage.hasNext())
                .currentPage(page)
                .build();
    }

    @Override
    public DiscussionPageResponseDTO getPopularDiscussions(String username, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        Page<Discussion> discussionPage = discussionRepository
                .findPopularDiscussions(pageable);

        List<DiscussionResponseDTO> dtos = discussionPage.getContent().stream()
                .map(d -> mapToDTO(d, username))
                .collect(Collectors.toList());

        return DiscussionPageResponseDTO.builder()
                .discussions(dtos)
                .totalElements(discussionPage.getTotalElements())
                .hasMore(discussionPage.hasNext())
                .currentPage(page)
                .build();
    }

    @Override
    public DiscussionPageResponseDTO getFavouriteDiscussions(String username, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        Page<Discussion> discussionPage = discussionRepository
                .findByUserFavourite(username, pageable);

        List<DiscussionResponseDTO> dtos = discussionPage.getContent().stream()
                .map(d -> mapToDTO(d, username))
                .collect(Collectors.toList());

        return DiscussionPageResponseDTO.builder()
                .discussions(dtos)
                .totalElements(discussionPage.getTotalElements())
                .hasMore(discussionPage.hasNext())
                .currentPage(page)
                .build();
    }

    /**
     * Gets a single discussion by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public DiscussionResponseDTO getById(String username, Long discussionId) {
        Discussion discussion = discussionRepository.findByIdAndIsActiveTrue(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("discussion.not_found"));
        return mapToDTO(discussion, username);
    }



    /**
     * Creates a new public discussion with the first message embedded in description.
     * Tags are resolved internally within this transaction.
     */
    @Override
    @Transactional
    public DiscussionResponseDTO create(String username, DiscussionCreateRequestDTO request) {
        User owner = findUserByUsername(username);

        // Resolve tags within the same persistence context
        Set<Tag> managedTags = resolveTagsByName(request.getTags());

        Discussion discussion = Discussion.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .owner(owner)
                .tags(managedTags)
                .messageCount(0)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        Discussion saved = discussionRepository.save(discussion);
        entityManager.flush();
        return mapToDTO(saved, username);
    }


    /**
     * Maps a Discussion entity to DTO, determining user participation type.
     */
    private DiscussionResponseDTO mapToDTO(Discussion discussion, String username) {
        boolean isFav = isFavourite(username, discussion.getId());
        String participationType = determineParticipationType(discussion, username, isFav);
        int favouriteCount = userDiscussionFavRepository.countByDiscussionIdAndIsActiveTrue(discussion.getId());
        int likeCount = discussionMessageRepository.countLikesByDiscussionId(discussion.getId());

        return DiscussionMapper.toResponseDTO(discussion, isFav, participationType, favouriteCount, likeCount);
    }

    /**
     * Determines how the user participates in the discussion.
     */
    private String determineParticipationType(Discussion discussion, String username, boolean isFav) {
        if (discussion.getOwner().getUsername().equals(username)) {
            return "OWNER";
        }
        // Check if user has sent messages in this discussion
        Long messageCount = (Long) entityManager.createQuery(
                        "SELECT COUNT(dm) FROM DiscussionMessage dm " +
                                "WHERE dm.discussion.id = :discussionId AND dm.sender.username = :username AND dm.isActive = true")
                .setParameter("discussionId", discussion.getId())
                .setParameter("username", username)
                .getSingleResult();
        if (messageCount > 0) {
            return "MESSAGED";
        }
        if (isFav) {
            return "FAVOURITE";
        }
        return "NONE";
    }


    /**
     * Resolves tag names to managed Tag entities within the current persistence context.
     * Creates any tags that don't exist yet.
     */
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

        // Find existing tags in the current persistence context
        List<Tag> existing = entityManager.createQuery(
                        "SELECT t FROM Tag t WHERE t.name IN :names AND t.isActive = true", Tag.class)
                .setParameter("names", normalized)
                .getResultList();

        Set<String> existingNames = existing.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        Set<Tag> result = new HashSet<>(existing);

        // Create missing tags
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


    /**
     * Gets paginated root-level messages for a discussion, with nested children.
     * Root messages are ordered by like count (most liked first).
     * Children are loaded recursively (1 level deep) and also ordered by likes.
     */
    @Override
    @Transactional(readOnly = true)
    public DiscussionMessagePageResponseDTO getMessages(Long discussionId, String username, int page) {
        // Verify discussion exists
        Discussion discussion = entityManager.find(Discussion.class, discussionId);
        if (discussion == null || !discussion.getIsActive()) {
            throw new ResourceNotFoundException("discussion.not_found");
        }

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<DiscussionMessage> rootMessages = discussionMessageRepository
                .findRootMessagesByDiscussion(discussionId, pageable);

        List<DiscussionMessageResponseDTO> dtos = rootMessages.getContent().stream()
                .map(msg -> mapMessageToDTO(msg, username))
                .collect(Collectors.toList());

        return DiscussionMessagePageResponseDTO.builder()
                .messages(dtos)
                .totalElements(rootMessages.getTotalElements())
                .hasMore(rootMessages.hasNext())
                .currentPage(page)
                .build();
    }

    /**
     * Creates a new message in a discussion.
     * Validates that the discussion is not closed.
     * Increments the discussion's message count.
     */
    @Override
    @Transactional
    public DiscussionMessageResponseDTO createMessage(
            Long discussionId,
            String username,
            DiscussionMessageCreateRequestDTO request,
            MultipartFile file) {

        Discussion discussion = entityManager.find(Discussion.class, discussionId);
        if (discussion == null || !discussion.getIsActive()) {
            throw new ResourceNotFoundException("discussion.not_found");
        }


        User sender = findUserByUsername(username);

        DiscussionMessage parentMessage = null;
        if (request.getParentMessageId() != null) {
            parentMessage = entityManager.find(DiscussionMessage.class, request.getParentMessageId());
            if (parentMessage == null || !parentMessage.getIsActive()) {
                throw new ResourceNotFoundException("discussion.message.not_found");
            }
            // Validate parent belongs to same discussion
            if (!parentMessage.getDiscussion().getId().equals(discussionId)) {
                throw new IllegalArgumentException("discussion.message.parent_mismatch");
            }
        }

        String mediaLink = uploadMessageMedia(file, sender.getUsername());

        DiscussionMessage message = DiscussionMessage.builder()
                .discussion(discussion)
                .sender(sender)
                .content(request.getContent())
                .link((mediaLink != null ? mediaLink : ""))
                .parentDiscussionMessage(parentMessage)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        DiscussionMessage saved = discussionMessageRepository.save(message);

        // Increment message count on discussion
        discussion.setMessageCount(discussion.getMessageCount() + 1);
        discussion.setLastUpdatedDatetime(LocalDateTime.now());
        entityManager.merge(discussion);

        return DiscussionMessageMapper.toResponseDTO(saved, 0, false, List.of());
    }

    /**
     * Maps a DiscussionMessage to DTO, recursively loading children (1 level).
     */
    private DiscussionMessageResponseDTO mapMessageToDTO(DiscussionMessage message, String username) {
        long likeCount = discussionMessageRepository.countLikesByMessageId(message.getId());
        boolean likedByUser = discussionMessageRepository.isLikedByUser(message.getId(), username);

        // Load children (replies) recursively
        List<DiscussionMessage> children = discussionMessageRepository.findChildMessages(message.getId());
        List<DiscussionMessageResponseDTO> childDTOs = children.stream()
                .map(child -> mapMessageToDTO(child, username))
                .collect(Collectors.toList());

        return DiscussionMessageMapper.toResponseDTO(message, likeCount, likedByUser, childDTOs);
    }






    /**
     * Adds a like to a message. Checks that user hasn't already liked it.
     */
    @Override
    @Transactional
    public MessageDTO like(String username, Long messageId) {
        if (messageLikeRepository.existsByUserUsernameAndMessageIdAndIsActiveTrue(username, messageId)) {
            throw new IllegalArgumentException("discussion.message.already_liked");
        }

        Optional<MessageLike> messageLike = messageLikeRepository.findByUserUsernameAndMessageIdAndIsActiveFalse(username, messageId);
        if (messageLike.isPresent()) {
            messageLikeRepository.save(messageLike.get());
            return MessageDTO.builder()
                    .success(true)
                    .message("discussion.message.like_added")
                    .build();
        }



        User user = findUserByUsername(username);
        DiscussionMessage message = entityManager.find(DiscussionMessage.class, messageId);
        if (message == null || !message.getIsActive()) {
            throw new ResourceNotFoundException("discussion.message.not_found");
        }

        MessageLike like = MessageLike.builder()
                .user(user)
                .message(message)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        messageLikeRepository.save(like);

        pointsService.addPoints(message.getSender(), 1L);

        return MessageDTO.builder()
                .success(true)
                .message("discussion.message.like_added")
                .build();
    }

    /**
     * Removes a like from a message. Performs soft-delete.
     */
    @Override
    @Transactional
    public MessageDTO unlike(String username, Long messageId) {
        Optional<MessageLike> likeOpt = messageLikeRepository
                .findByUserUsernameAndMessageIdAndIsActiveTrue(username, messageId);

        if (likeOpt.isEmpty()) {
            throw new IllegalArgumentException("discussion.message.not_liked");
        }

        MessageLike like = likeOpt.get();
        User messageSender = like.getMessage().getSender();

        like.setIsActive(false);
        like.setLastUpdatedDatetime(LocalDateTime.now());
        messageLikeRepository.save(like);

        pointsService.deductPoints(messageSender, 1L);

        return MessageDTO.builder()
                .success(true)
                .message("discussion.message.like_removed")
                .build();
    }

    /**
     * Adds a discussion to the user's favourites.
     */
    @Override
    @Transactional
    public MessageDTO addFavourite(String username, Long discussionId) {
        if (userDiscussionFavRepository.existsByUserUsernameAndDiscussionIdAndIsActiveTrue(username, discussionId)) {
            throw new IllegalArgumentException("discussion.already_favourite");
        }

        User user = findUserByUsername(username);
        Discussion discussion = entityManager.find(Discussion.class, discussionId);
        if (discussion == null || !discussion.getIsActive()) {
            throw new ResourceNotFoundException("discussion.not_found");
        }

        UserDiscussionFav fav = UserDiscussionFav.builder()
                .user(user)
                .discussion(discussion)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        userDiscussionFavRepository.save(fav);

        pointsService.addPoints(discussion.getOwner(), 5L);

        return MessageDTO.builder()
                .success(true)
                .message("discussion.favourite_added")
                .build();
    }

    /**
     * Removes a discussion from the user's favourites (soft-delete).
     */
    @Override
    @Transactional
    public MessageDTO removeFavourite(String username, Long discussionId) {
        Optional<UserDiscussionFav> favOpt = userDiscussionFavRepository
                .findByUserUsernameAndDiscussionIdAndIsActiveTrue(username, discussionId);

        if (favOpt.isEmpty()) {
            throw new IllegalArgumentException("discussion.not_favourite");
        }

        UserDiscussionFav fav = favOpt.get();
        User discussionOwner = fav.getDiscussion().getOwner();

        fav.setIsActive(false);
        fav.setLastUpdatedDatetime(LocalDateTime.now());
        userDiscussionFavRepository.save(fav);

        pointsService.deductPoints(discussionOwner, 5L);

        return MessageDTO.builder()
                .success(true)
                .message("discussion.favourite_removed")
                .build();
    }

    /**
     * Checks if a user has favourited a discussion.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isFavourite(String username, Long discussionId) {
        return userDiscussionFavRepository
                .existsByUserUsernameAndDiscussionIdAndIsActiveTrue(username, discussionId);
    }

    private String uploadMessageMedia(MultipartFile file, String username) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);


            String url = mediaServiceUrl + "/upload-image/message";
            logger.info("Calling media service at URL: {}", url);


            ImageUploadResponseDTO response = restTemplate.postForObject(url, requestEntity, ImageUploadResponseDTO.class);
            if (response != null && response.getUrls() != null) {
                logger.info("Received URLs from media service for message: {}", response.getUrls());
                return response.getUrls().get("original");
            } else {
                logger.warn("Media service returned a null or empty response for message file.");
            }


        } catch (Exception e) {
            logger.error("🚨 Failed to call media service to upload image for message sent by {}", username, e);
        }
        return null;
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
}

