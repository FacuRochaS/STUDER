package facu.studer.repositories;

import facu.studer.entities.discussions.UserDiscussionFav;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for UserDiscussionFav entity.
 * Manages user favourite discussions.
 */
@Repository
public interface UserDiscussionFavRepository extends JpaRepository<UserDiscussionFav, Long> {

    /**
     * Checks if a user has favourited a discussion.
     */
    boolean existsByUserUsernameAndDiscussionIdAndIsActiveTrue(String username, Long discussionId);

    /**
     * Finds a favourite entry for a user and discussion.
     */
    Optional<UserDiscussionFav> findByUserUsernameAndDiscussionIdAndIsActiveTrue(String username, Long discussionId);

    /**
     * Finds all favourite discussions for a user (paginated).
     */
    Page<UserDiscussionFav> findByUserUsernameAndIsActiveTrue(String username, Pageable pageable);
}

