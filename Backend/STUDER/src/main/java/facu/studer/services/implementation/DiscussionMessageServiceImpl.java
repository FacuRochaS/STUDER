package facu.studer.services.implementation;

import facu.studer.DTOs.discussions.DiscussionMessageCreateRequestDTO;
import facu.studer.DTOs.discussions.DiscussionMessagePageResponseDTO;
import facu.studer.DTOs.discussions.DiscussionMessageResponseDTO;
import facu.studer.entities.User;
import facu.studer.entities.discussions.Discussion;
import facu.studer.entities.discussions.DiscussionMessage;
import facu.studer.exceptions.DiscussionClosedException;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.mappers.DiscussionMessageMapper;
import facu.studer.repositories.DiscussionMessageRepository;
import facu.studer.services.DiscussionMessageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of DiscussionMessageService.
 * Uses only DiscussionMessageRepository (respects 1-repo-per-service).
 * Uses EntityManager for lookups of Discussion, User, and parent messages.
 */
@Service
public class DiscussionMessageServiceImpl implements DiscussionMessageService {

    private static final int PAGE_SIZE = 10;

    private final DiscussionMessageRepository discussionMessageRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public DiscussionMessageServiceImpl(DiscussionMessageRepository discussionMessageRepository) {
        this.discussionMessageRepository = discussionMessageRepository;
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
            DiscussionMessageCreateRequestDTO request) {

        Discussion discussion = entityManager.find(Discussion.class, discussionId);
        if (discussion == null || !discussion.getIsActive()) {
            throw new ResourceNotFoundException("discussion.not_found");
        }

        if (discussion.isClosed()) {
            throw new DiscussionClosedException("discussion.closed");
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

        DiscussionMessage message = DiscussionMessage.builder()
                .discussion(discussion)
                .sender(sender)
                .content(request.getContent())
                .imageRef(request.getImageRef())
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

