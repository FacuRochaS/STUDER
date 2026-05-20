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
 * Entity representing a message in a discussion or as a reply.
 */
@Entity
@Table(name = "discussion_messages")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionMessage extends BaseEntity {

    /**
     * discussion to which this message belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discussion_id")
    private Discussion discussion;

    /**
     * User who sent the message.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    /**
     * Content of the message.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Parent message (for replies/threads). Nullable.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id")
    private DiscussionMessage parentDiscussionMessage;

    /**
     * Reference to an image stored in external microservice. Nullable.
     */
    private String imageRef;

}
