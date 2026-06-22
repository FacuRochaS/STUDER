package facu.studer.entities.feed;

import facu.studer.entities.BaseEntity;
import facu.studer.entities.Tag;
import facu.studer.entities.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * Entity representing a post.
 */
@Entity
@Table(name = "posts")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {

    /**
     * User who made the post.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Content of the post.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * link to photo.
     */
    @Column(nullable = false)
    private String link;

    /**
     * Tags associated with the post.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "discussion_tags",
            joinColumns = @JoinColumn(name = "discussion_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;
}
