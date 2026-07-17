package facu.studer.repositories.courses;

import facu.studer.entities.courses.UserCourseBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCourseBlockRepository extends JpaRepository<UserCourseBlock, Long> {
    Optional<UserCourseBlock> findByUserUsernameAndCourseBlockIdAndIsActiveTrue(String username, Long courseBlockId);
}
