package facu.studer.entities.messages;

import facu.studer.entities.BaseEntity;
import facu.studer.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a chat between users.
 */
@Entity
@Table(name = "chats")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Chat extends BaseEntity {
    /**
     * Chat user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_1_id", nullable = false)
    private User user1;

    /**
     * Chat user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_2_id", nullable = false)
    private User user2;
}
