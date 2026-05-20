package facu.studer.repositories;

import facu.studer.entities.LinkedType;
import facu.studer.entities.User;
import facu.studer.entities.notifications.UserNotification;
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
 * Repository for UserNotification entity.
 * Provides paginated and filtered access to user notifications.
 */
@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    List<UserNotification> findByUser(User user);

    List<UserNotification> findByUserAndReadFalse(User user);

    /**
     * Finds paginated notifications for a user with optional filters.
     * Only returns notifications where availableAt <= now and notification is active.
     *
     * @param username  the username
     * @param now       current timestamp (filters out future availableAt)
     * @param type      optional LinkedType filter (null = no filter)
     * @param read      optional read status filter (null = no filter)
     * @param since     optional date-since filter (null = no filter)
     * @param pageable  pagination info
     * @return page of UserNotification
     */
    @Query("SELECT un FROM UserNotification un " +
            "JOIN FETCH un.notification n " +
            "WHERE un.user.username = :username " +
            "AND un.isActive = true " +
            "AND n.isActive = true " +
            "AND (n.availableAt IS NULL OR n.availableAt <= :now) " +
            "AND (:type IS NULL OR n.type = :type) " +
            "AND (:read IS NULL OR un.read = :read) " +
            "AND (:since IS NULL OR n.createdDatetime >= :since) " +
            "ORDER BY n.createdDatetime DESC")
    Page<UserNotification> findFilteredNotifications(
            @Param("username") String username,
            @Param("now") LocalDateTime now,
            @Param("type") LinkedType type,
            @Param("read") Boolean read,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    /**
     * Finds a UserNotification by ID with the associated notification eagerly loaded.
     */
    @Query("SELECT un FROM UserNotification un " +
            "JOIN FETCH un.notification n " +
            "WHERE un.id = :id AND un.isActive = true AND n.isActive = true")
    Optional<UserNotification> findByIdWithNotification(@Param("id") Long id);
}

