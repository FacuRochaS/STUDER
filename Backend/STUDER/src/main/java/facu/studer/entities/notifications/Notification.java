package facu.studer.entities.notifications;


import facu.studer.entities.BaseEntity;
import facu.studer.entities.LinkedType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity representing a notification.
 */
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class Notification extends BaseEntity {

    /** Notification title. */
    private String title;

    /** Notification message. */
    private String message;

    /** Notification type. */
    @Enumerated(EnumType.STRING)
    private LinkedType type;

    /** Notification linked id entity. */
    private Long linkedId;

}

