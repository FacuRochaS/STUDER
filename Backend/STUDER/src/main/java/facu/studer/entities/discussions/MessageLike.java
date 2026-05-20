package facu.studer.entities.discussions;

import facu.studer.entities.BaseEntity;
import facu.studer.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a like on a discussion message.
 * Each user can like a message only once (unique constraint on user + message).
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "message_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "message_id"})
})
public class MessageLike extends BaseEntity {

    /** The user who liked the message. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /** The message that was liked. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id")
    private DiscussionMessage message;

}

