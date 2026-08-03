package facu.studer.repositories.contest;

import facu.studer.entities.contest.CourseRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRatingRepository extends JpaRepository<CourseRating, Long> {
    boolean existsByUserUsernameAndCourseIdAndIsActiveTrue(String username, Long courseId);
    Optional<CourseRating> findByUserUsernameAndCourseIdAndIsActiveTrue(String username, Long courseId);
    Optional<CourseRating> findByUserUsernameAndCourseId(String username, Long courseId);
    long countByCourseIdAndIsActiveTrue(Long courseId);
}
