package facu.studer.repositories.contest;

import facu.studer.entities.contest.Contest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {
    Page<Contest> findByStatusOrderByStartDateDesc(String status, Pageable pageable);
    Page<Contest> findAllByOrderByStartDateDesc(Pageable pageable);
    List<Contest> findByStatusOrderByStartDateDesc(String status);
    long countByStatus(String status);

    @Query("SELECT c FROM Contest c WHERE c.startDate <= :now AND c.endDate >= :now AND c.isActive = true ORDER BY c.startDate ASC")
    List<Contest> findActiveContests(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM Contest c WHERE c.startDate > :now AND c.isActive = true ORDER BY c.startDate ASC")
    List<Contest> findUpcomingContests(@Param("now") LocalDateTime now);
}
