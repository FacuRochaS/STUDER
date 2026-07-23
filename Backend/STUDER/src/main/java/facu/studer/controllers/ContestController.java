package facu.studer.controllers;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.contest.*;
import facu.studer.DTOs.courses.CourseCreateRequestDTO;
import facu.studer.DTOs.courses.CourseResponseDTO;
import facu.studer.security.SecurityUtils;
import facu.studer.services.ContestService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contests")
public class ContestController {

    private final ContestService contestService;
    private final SecurityUtils securityUtils;

    public ContestController(ContestService contestService, SecurityUtils securityUtils) {
        this.contestService = contestService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<Page<ContestResponseDTO>> listContests(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String username = securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(contestService.listContests(status, PageRequest.of(page, size)));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ContestResponseDTO>> getActiveContests() {
        return ResponseEntity.ok(contestService.getActiveContests());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ContestResponseDTO>> getUpcomingContests() {
        return ResponseEntity.ok(contestService.getUpcomingContests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContestResponseDTO> getContest(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getContest(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<CourseResponseDTO> submitCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseCreateRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(contestService.submitCourseForContest(username, id, request));
    }

    @GetMapping("/{id}/validate/random")
    public ResponseEntity<ContestCourseResponseDTO> getRandomCourse(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(contestService.getRandomCourseForValidation(username, id));
    }

    @PostMapping("/validate")
    public ResponseEntity<MessageDTO> rateCourse(@Valid @RequestBody CourseRatingRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(contestService.rateCourse(username, request));
    }

    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDTO>> getLeaderboard(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getLeaderboard(id));
    }

    @GetMapping("/achievements")
    public ResponseEntity<List<AchievementResponseDTO>> getUserAchievements() {
        String username = securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(contestService.getUserAchievements(username));
    }

    @GetMapping("/achievements/all")
    public ResponseEntity<List<AchievementResponseDTO>> getAllAchievements() {
        return ResponseEntity.ok(contestService.getAllAchievements());
    }
}
