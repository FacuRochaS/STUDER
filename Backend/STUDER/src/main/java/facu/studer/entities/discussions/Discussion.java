package facu.studer.entities.discussions;

import facu.studer.entities.BaseEntity;
import facu.studer.entities.Tag;
import facu.studer.entities.User;
import facu.studer.entities.curses.Course;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entity representing a forum or post (main publication).
 */
@Entity
@Table(name = "discussions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Discussion extends BaseEntity {

    /**
     * Title of the discussion/post.
     */
    @Column(nullable = false)
    private String title;

    /**
     * Description or main content.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Owner/creator of the discussion.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    /**
     * Tags associated with the discussion.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "discussion_tags",
            joinColumns = @JoinColumn(name = "discussion_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

    /**
     * Whether the discussion is closed for new messages.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean closed = false;

    /**
     * When the discussion was closed (nullable).
     */
    private LocalDateTime closedAt;

    /**
     * Number of messages for filtering/performance.
     */
    @Column(nullable = false)
    @Builder.Default
    private int messageCount = 0;

    /**
     *  The course this discussionbelongs to. If null, the discussion is public.
     * If not null, the discussion is private to the group/course.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;


}
