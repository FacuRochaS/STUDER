package facu.studer.repositories;

import facu.studer.entities.discussions.MessageLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for MessageLike entity.
 * Manages user likes on discussion messages.
 */
@Repository
public interface MessageLikeRepository extends JpaRepository<MessageLike, Long> {

    /**
     * Checks if a user has liked a specific message.
     */
    boolean existsByUserUsernameAndMessageIdAndIsActiveTrue(String username, Long messageId);

    /**
     * Finds a like entry for a user and message.
     */
    Optional<MessageLike> findByUserUsernameAndMessageIdAndIsActiveTrue(String username, Long messageId);

    /**
     * Counts active likes for a message.
     */
    long countByMessageIdAndIsActiveTrue(Long messageId);
}

