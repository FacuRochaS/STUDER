package facu.studer.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


/**
 * Entity representing a tag for forums/posts.
 */
@Entity
@Table(name = "tags")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Tag extends BaseEntity  {

    /**
     * Name of the tag.
     */
    @Column(nullable = false, unique = true)
    private String name;

}
