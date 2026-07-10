package facu.studer.repositories.courses;

import facu.studer.entities.courses.UserCourseBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCourseBlockRepository extends JpaRepository<UserCourseBlock, Long> {
}
