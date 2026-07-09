package facu.studer.entities.courses;

import facu.studer.entities.BaseEntity;
import facu.studer.entities.discussions.Discussion;
import facu.studer.entities.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_course_favs")
public class UserCourseFav extends BaseEntity {

    /** Associated user. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Associated course. */
    @ManyToOne()
    @JoinColumn(name = "course_id")
    private Course course;


}