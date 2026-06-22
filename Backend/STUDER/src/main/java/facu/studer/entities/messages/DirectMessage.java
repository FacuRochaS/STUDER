package facu.studer.entities.messages;

import facu.studer.entities.BaseEntity;
import facu.studer.entities.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


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
     * Reference to the chat.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    
    /**
     * User who sends the message.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * Content of the message.
     */
    @Column(nullable = false)
    private String content;

    /**
     * link to photo.
     */
    @Column(nullable = false)
    private String link;


    /**
     * Reference to the message being replied to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private DirectMessage replyTo;

    /**
     * Whether the message has been read.
     */
    @Column(nullable = false)
    private Boolean isRead;

}
