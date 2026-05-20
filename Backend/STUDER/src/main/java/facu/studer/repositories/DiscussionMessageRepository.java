package facu.studer.repositories;

import facu.studer.entities.discussions.DiscussionMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DiscussionMessage entity.
 * Provides paginated queries for messages with like counts and hierarchy support.
 */
@Repository
public interface DiscussionMessageRepository extends JpaRepository<DiscussionMessage, Long> {

    /**
     * Finds root-level messages (no parent) for a discussion, ordered by like count descending.
     */
    @Query("SELECT dm FROM DiscussionMessage dm " +
            "LEFT JOIN MessageLike ml ON ml.message = dm " +
            "WHERE dm.discussion.id = :discussionId " +
            "AND dm.parentDiscussionMessage IS NULL " +
            "AND dm.isActive = true " +
            "GROUP BY dm " +
            "ORDER BY COUNT(ml) DESC, dm.createdDatetime ASC")
    Page<DiscussionMessage> findRootMessagesByDiscussion(
            @Param("discussionId") Long discussionId,
            Pageable pageable);

    /**
     * Finds child messages for a parent message, ordered by like count descending.
     */
    @Query("SELECT dm FROM DiscussionMessage dm " +
            "LEFT JOIN MessageLike ml ON ml.message = dm " +
            "WHERE dm.parentDiscussionMessage.id = :parentId " +
            "AND dm.isActive = true " +
            "GROUP BY dm " +
            "ORDER BY COUNT(ml) DESC, dm.createdDatetime ASC")
    List<DiscussionMessage> findChildMessages(@Param("parentId") Long parentId);

    /**
     * Counts likes for a specific message.
     */
    @Query("SELECT COUNT(ml) FROM MessageLike ml WHERE ml.message.id = :messageId AND ml.isActive = true")
    long countLikesByMessageId(@Param("messageId") Long messageId);

    /**
     * Checks if a user has liked a specific message.
     */
    @Query("SELECT CASE WHEN COUNT(ml) > 0 THEN true ELSE false END " +
            "FROM MessageLike ml WHERE ml.message.id = :messageId AND ml.user.username = :username AND ml.isActive = true")
    boolean isLikedByUser(@Param("messageId") Long messageId, @Param("username") String username);
}

