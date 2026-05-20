package facu.studer.services.implementation;

import facu.studer.DTOs.discussions.MessageResponseDTO;
import facu.studer.entities.User;
import facu.studer.entities.discussions.DiscussionMessage;
import facu.studer.entities.discussions.MessageLike;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.repositories.MessageLikeRepository;
import facu.studer.services.MessageLikeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementation of MessageLikeService.
 * Uses only MessageLikeRepository (respects 1-repo-per-service).
 * Uses EntityManager for lookups of User and DiscussionMessage.
 */
@Service
public class MessageLikeServiceImpl implements MessageLikeService {

    private final MessageLikeRepository messageLikeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public MessageLikeServiceImpl(MessageLikeRepository messageLikeRepository) {
        this.messageLikeRepository = messageLikeRepository;
    }

    /**
     * Adds a like to a message. Checks that user hasn't already liked it.
     */
    @Override
    @Transactional
    public MessageResponseDTO like(String username, Long messageId) {
        if (messageLikeRepository.existsByUserUsernameAndMessageIdAndIsActiveTrue(username, messageId)) {
            throw new IllegalArgumentException("discussion.message.already_liked");
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

        return MessageResponseDTO.builder()
                .success(true)
                .message("discussion.message.like_added")
                .build();
    }

    /**
     * Removes a like from a message. Performs soft-delete.
     */
    @Override
    @Transactional
    public MessageResponseDTO unlike(String username, Long messageId) {
        Optional<MessageLike> likeOpt = messageLikeRepository
                .findByUserUsernameAndMessageIdAndIsActiveTrue(username, messageId);

        if (likeOpt.isEmpty()) {
            throw new IllegalArgumentException("discussion.message.not_liked");
        }

        MessageLike like = likeOpt.get();
        like.setIsActive(false);
        like.setLastUpdatedDatetime(LocalDateTime.now());
        messageLikeRepository.save(like);

        return MessageResponseDTO.builder()
                .success(true)
                .message("discussion.message.like_removed")
                .build();
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

