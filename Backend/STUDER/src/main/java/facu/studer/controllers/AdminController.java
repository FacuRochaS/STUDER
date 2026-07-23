package facu.studer.controllers;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.contest.*;
import facu.studer.security.SecurityUtils;
import facu.studer.services.ContestService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final ContestService contestService;
    private final SecurityUtils securityUtils;

    @PersistenceContext
    private EntityManager entityManager;

    public AdminController(ContestService contestService, SecurityUtils securityUtils) {
        this.contestService = contestService;
        this.securityUtils = securityUtils;
    }

    // ===== DASHBOARD =====

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardMetricsDTO> getDashboard() {
        securityUtils.requireCurrentUsername();

        long totalUsers = count("SELECT COUNT(u) FROM User u WHERE u.isActive = true");
        long activeToday = count("SELECT COUNT(u) FROM User u WHERE u.lastConnectionTime >= :since AND u.isActive = true",
                LocalDateTime.now().minusHours(24));
        long activeThisWeek = count("SELECT COUNT(u) FROM User u WHERE u.lastConnectionTime >= :since AND u.isActive = true",
                LocalDateTime.now().minusDays(7));
        long newRegistrations = count("SELECT COUNT(u) FROM User u WHERE u.createdDatetime >= :since AND u.isActive = true",
                LocalDateTime.now().minusDays(7));
        long totalBlocks = count("SELECT COUNT(b) FROM Block b WHERE b.isActive = true");
        long totalCourses = count("SELECT COUNT(c) FROM Course c WHERE c.isActive = true");
        long totalForks = count("SELECT COUNT(b) FROM Block b WHERE b.isFork = true AND b.isActive = true");
        long totalVersions = count("SELECT COUNT(bv) FROM BlockVersion bv WHERE bv.isActive = true");
        long totalLikes = count("SELECT COUNT(l) FROM BlockLike l WHERE l.isActive = true");
        long totalComments = count("SELECT COUNT(m) FROM DiscussionMessage m WHERE m.isActive = true");
        long activeContests = count("SELECT COUNT(c) FROM Contest c WHERE c.status NOT IN ('RESULTS','CANCELLED') AND c.isActive = true");
        long finishedContests = count("SELECT COUNT(c) FROM Contest c WHERE c.status = 'RESULTS' AND c.isActive = true");

        List<DifficultyDistributionDTO> difficultyDist = List.of();
        List<TagDistributionDTO> topTags = List.of();
        List<UserReputationDTO> topUsers = List.of();

        try {
            difficultyDist = entityManager.createQuery(
                    "SELECT new facu.studer.DTOs.contest.DifficultyDistributionDTO(CAST(b.difficulty AS string), COUNT(b)) " +
                            "FROM Block b WHERE b.isActive = true GROUP BY b.difficulty ORDER BY COUNT(b) DESC",
                    DifficultyDistributionDTO.class).getResultList();

            topTags = entityManager.createQuery(
                            "SELECT new facu.studer.DTOs.contest.TagDistributionDTO(t.name, COUNT(bt)) " +
                                    "FROM Block b JOIN b.tags t WHERE b.isActive = true GROUP BY t.name ORDER BY COUNT(bt) DESC",
                            TagDistributionDTO.class)
                    .setMaxResults(10).getResultList();

            topUsers = entityManager.createQuery(
                            "SELECT new facu.studer.DTOs.contest.UserReputationDTO(u.id, u.username, u.firstName, u.lastName, u.profilePictureThumbnailUrl, u.points) " +
                                    "FROM User u WHERE u.isActive = true ORDER BY u.points DESC",
                            UserReputationDTO.class)
                    .setMaxResults(10).getResultList();
        } catch (Exception ignored) {}

        return ResponseEntity.ok(DashboardMetricsDTO.builder()
                .totalUsers(totalUsers).activeToday(activeToday).activeThisWeek(activeThisWeek)
                .newRegistrations(newRegistrations).totalBlocks(totalBlocks).totalCourses(totalCourses)
                .totalForks(totalForks).totalVersions(totalVersions).totalLikes(totalLikes)
                .totalComments(totalComments).activeContests(activeContests).finishedContests(finishedContests)
                .difficultyDistribution(difficultyDist).topTags(topTags).topUsers(topUsers)
                .build());
    }

    // ===== CONTEST MANAGEMENT =====

    @PostMapping("/contests")
    public ResponseEntity<ContestResponseDTO> createContest(@Valid @RequestBody ContestCreateRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(contestService.createContest(username, request));
    }

    @PutMapping("/contests/{id}")
    public ResponseEntity<ContestResponseDTO> updateContest(@PathVariable Long id, @Valid @RequestBody ContestCreateRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(contestService.updateContest(username, id, request));
    }

    @DeleteMapping("/contests/{id}")
    public ResponseEntity<MessageDTO> deleteContest(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(contestService.deleteContest(username, id));
    }

    @PatchMapping("/contests/{id}/status")
    public ResponseEntity<MessageDTO> changeContestStatus(@PathVariable Long id, @RequestBody StatusChangeRequest request) {
        String username = securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(contestService.changeContestStatus(username, id, request.getStatus()));
    }

    @PostMapping("/contests/{id}/finish")
    public ResponseEntity<MessageDTO> finishContest(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(contestService.finishContest(username, id));
    }

    private long count(String jpql, Object... params) {
        var q = entityManager.createQuery(jpql, Long.class);
        if (params.length > 0 && params[0] instanceof LocalDateTime since) {
            q.setParameter("since", since);
        }
        return q.getSingleResult();
    }
}
