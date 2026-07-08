package facu.studer.repositories.discussion;

import facu.studer.entities.discussions.Discussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Discussion entity.
 * Provides paginated queries for public discussions with dynamic filters.
 */
@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

    /**
     * Finds public discussions (course IS NULL) with optional filters,
     * ordered by recent activity (message count in last X hours).
     *
     * @param tagNames   optional tag name filter (empty list = no filter)
     * @param since      optional created-since filter
     * @param pageable   pagination info
     * @return page of Discussion
     */
    @Query("SELECT DISTINCT d FROM Discussion d " +
            "LEFT JOIN d.tags t " +
            "WHERE d.isActive = true " +
            "AND (:since IS NULL OR d.createdDatetime >= :since) " +
            "AND (:hasTags = false OR t.name IN :tagNames) " +
            "ORDER BY d.createdDatetime DESC")
    Page<Discussion> findPublicDiscussions(
            @Param("hasTags") boolean hasTags,
            @Param("tagNames") List<String> tagNames,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    /**
     * Finds public discussions ordered by recent activity
     * (number of messages created in the last N hours).
     */
    @Query("SELECT d FROM Discussion d " +
            "LEFT JOIN d.tags t " +
            "LEFT JOIN DiscussionMessage dm ON dm.discussion = d AND dm.createdDatetime >= :activitySince " +
            "WHERE d.isActive = true " +
            "AND (:since IS NULL OR d.createdDatetime >= :since) " +
            "AND (:hasTags = false OR t.name IN :tagNames) " +
            "GROUP BY d " +
            "ORDER BY COUNT(dm) DESC, d.createdDatetime DESC")
    Page<Discussion> findPublicDiscussionsByActivity(
            @Param("hasTags") boolean hasTags,
            @Param("tagNames") List<String> tagNames,
            @Param("since") LocalDateTime since,
            @Param("activitySince") LocalDateTime activitySince,
            Pageable pageable);

    @Query(value = "SELECT d FROM Discussion d " +

            "LEFT JOIN DiscussionMessage dm ON dm.discussion = d AND dm.sender.username = :username " +
            "LEFT JOIN UserDiscussionFav u ON u.discussion = d AND u.user.username = :username " +
            "WHERE d.isActive = true " +
            "AND u.isActive= true " +
            "AND (d.owner.username = :username OR dm.id IS NOT NULL OR u.id IS NOT NULL) " +
            "GROUP BY d " +
            "ORDER BY MAX(" +
            "   GREATEST(" +
            "       COALESCE(dm.createdDatetime, d.createdDatetime), " +
            "       COALESCE(u.createdDatetime, d.createdDatetime) " +
            "   )" +
            ") DESC",
            countQuery = "SELECT COUNT(DISTINCT d) FROM Discussion d " +
                    "LEFT JOIN DiscussionMessage dm ON dm.discussion = d AND dm.sender.username = :username " +
                    "LEFT JOIN UserDiscussionFav u ON u.discussion = d AND u.user.username = :username " +
                    "WHERE d.isActive = true " +
                    "AND (d.owner.username = :username OR dm.id IS NOT NULL OR u.id IS NOT NULL)")
    Page<Discussion> findByUserParticipation(
            @Param("username") String username,
            Pageable pageable);


    /**
     * Finds discussions where the user is the owner.
     */
    @Query("SELECT DISTINCT d FROM Discussion d " +
            "WHERE d.isActive = true " +
            "AND (d.owner.username = :username )")
    Page<Discussion> findByUsername(
            @Param("username") String username,
            Pageable pageable);

    /**
     * Finds a discussion by ID only if active.
     */
    Optional<Discussion> findByIdAndIsActiveTrue(Long id);


    /**
     *
     */
    @Query(value = "SELECT d FROM Discussion d " +
            "LEFT JOIN MessageLike l ON l.message.discussion = d " +
            "WHERE d.isActive = true " +
            "AND l.isActive = true " +
            "GROUP BY d " +
            "ORDER BY COUNT(l) DESC, d.createdDatetime DESC",
            countQuery = "SELECT COUNT(d) FROM Discussion d WHERE d.isActive = true")
    Page<Discussion> findPopularDiscussions(Pageable pageable);


    /**
     *
     */
    @Query("SELECT d FROM Discussion d " +
            "JOIN UserDiscussionFav u ON u.discussion = d " +
            "WHERE d.isActive = true " +
            "AND u.isActive = true " +
            "AND (u.user.username = :username)")
    Page<Discussion> findByUserFavourite(
            @Param("username") String username,
            Pageable pageable);
}

