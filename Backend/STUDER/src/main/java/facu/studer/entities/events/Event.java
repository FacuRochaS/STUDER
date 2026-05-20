package facu.studer.entities.events;

import facu.studer.entities.BaseEntity;
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
 * Entity representing an Event.
 */
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class Event extends BaseEntity {

    /** Event title. */
    private String title;

    /** Event message. */
    private String message;

    /** Event type. */
    @Enumerated(EnumType.STRING)
    private EventType type;

    /** Event linked id entity. */
    private Long linkedId;

    /** Event start time. */
    private LocalDateTime startTime;

    /** Event end time. */
    private LocalDateTime endTime;

}
