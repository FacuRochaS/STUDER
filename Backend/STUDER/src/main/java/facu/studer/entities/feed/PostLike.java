package facu.studer.entities.feed;

import facu.studer.entities.BaseEntity;
import facu.studer.entities.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a like on a post.
 * Each user can like a post only once (unique constraint on user + post).
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
public class PostLike extends BaseEntity {

    /** The user who liked the post. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /** The post that was liked. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;
}
