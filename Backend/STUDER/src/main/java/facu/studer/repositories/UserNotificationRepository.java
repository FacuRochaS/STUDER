package facu.studer.repositories;

import facu.studer.entities.User;
import facu.studer.entities.notifications.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long>, JpaSpecificationExecutor<UserNotification> {

    List<UserNotification> findByUser(User user);

    List<UserNotification> findByUserAndReadFalse(User user);

    @Query("SELECT un FROM UserNotification un " +
            "JOIN FETCH un.notification n " +
            "WHERE un.id = :id AND un.isActive = true AND n.isActive = true")
    Optional<UserNotification> findByIdWithNotification(@Param("id") Long id);
}