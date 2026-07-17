package facu.studer.repositories.courses;

import facu.studer.entities.courses.UserCourseFav;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCourseFavRepository extends JpaRepository<UserCourseFav, Long> {
    boolean existsByUserUsernameAndCourseIdAndIsActiveTrue(String username, Long courseId);
    Optional<UserCourseFav> findByUserUsernameAndCourseIdAndIsActiveTrue(String username, Long courseId);
    long countByCourseIdAndIsActiveTrue(Long courseId);
}
