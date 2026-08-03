package facu.studer.repositories.contest;

import facu.studer.entities.contest.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserUsernameAndIsActiveTrue(String username);
    Optional<UserAchievement> findByUserUsernameAndAchievementIdAndIsActiveTrue(String username, Long achievementId);
    boolean existsByUserUsernameAndAchievementIdAndIsActiveTrue(String username, Long achievementId);
}
