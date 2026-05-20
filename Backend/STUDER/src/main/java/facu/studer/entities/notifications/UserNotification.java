package facu.studer.entities.notifications;

import facu.studer.entities.BaseEntity;
import facu.studer.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity representing a notification for a specific user.
 */
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_notifications")
public class UserNotification extends BaseEntity {

    /** Associated user. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Associated notification. */
    @ManyToOne()
    @JoinColumn(name = "notification_id")
    private Notification notification;

    /** Read status. */
    private boolean read;

    /** Timestamp when marked as read. */
    private LocalDateTime readAt;

}

