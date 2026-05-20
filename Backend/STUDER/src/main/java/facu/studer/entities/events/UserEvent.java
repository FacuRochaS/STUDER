package facu.studer.entities.events;

import facu.studer.entities.BaseEntity;
import facu.studer.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing an event for a specific user.
 */
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_events")
public class UserEvent extends BaseEntity {

    /** Associated user. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Associated event. */
    @ManyToOne()
    @JoinColumn(name = "event_id")
    private Event event;
}
