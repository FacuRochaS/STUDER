package facu.studer.entities.courses;

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

@Entity
@Table(name = "courses")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Course extends BaseEntity {
    /**
     * Owner.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Name of the course.
     */
    @Column(nullable = false)
    private String name;


    /**
     * Unique SLUG.
     */
    @Column(nullable = false, unique = true)
    private String slug;


    /**
     * Tags associated with the course.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "course_tags",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

    /**
     * Published status.
     */
    @Column(nullable = false)
    private Boolean published;

    /**
     * link to photo.
     */
    @Column(nullable = false)
    private String link;

}
