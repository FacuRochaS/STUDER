package facu.studer.entities.messages;

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
 * Entity representing a direct message between users.
 */
@Entity
@Table(name = "messages")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessage extends BaseEntity {
    /**
     * User who sends the message.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * User who receives the message.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    /**
     * Content of the message.
     */
    @Column(nullable = false)
    private String content;

    /**
     * Timestamp of when the message was sent.
     */
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

}
