package facu.studer.entities.curses;

import facu.studer.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a forum or post (main publication).
 */
@Entity
@Table(name = "courses")
@Getter
@Setter
@SuperBuilder
public class Course extends BaseEntity {
    public Course() {
        super();
    }
}
