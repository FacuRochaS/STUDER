package facu.studer.entities.courses;

import facu.studer.entities.BaseEntity;
import facu.studer.entities.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_course_blocks")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserCourseBlock extends BaseEntity {
    /** Associated user. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Course.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_block_id", nullable = false)
    private CourseBlock courseBlock;


    private Boolean completed;

    private Long duration;

    private Integer attempts;
}
