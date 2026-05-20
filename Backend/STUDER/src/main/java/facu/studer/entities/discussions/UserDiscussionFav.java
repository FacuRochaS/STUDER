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
 * Entity representing a saved as favourite discussion for a specific user.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_discussion_favs")
public class UserDiscussionFav extends BaseEntity {

    /** Associated user. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Associated discussion. */
    @ManyToOne()
    @JoinColumn(name = "discussion_id")
    private Discussion discussion;


}
