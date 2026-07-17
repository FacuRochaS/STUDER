package facu.studer.controllers;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.contest.*;
import facu.studer.DTOs.courses.CourseCreateRequestDTO;
import facu.studer.DTOs.courses.CourseResponseDTO;
import facu.studer.security.SecurityUtils;
import facu.studer.services.ContestService;
import jakarta.validation.Valid;
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

    @GetMapping("/{id}")
    public ResponseEntity<ContestResponseDTO> getContest(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        ContestResponseDTO response = contestService.getContest(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<CourseResponseDTO> submitCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseCreateRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        CourseResponseDTO response = contestService.submitCourseForContest(username, id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/validate/random")
    public ResponseEntity<ContestCourseResponseDTO> getRandomCourse(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        ContestCourseResponseDTO response = contestService.getRandomCourseForValidation(username, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<MessageDTO> rateCourse(
            @Valid @RequestBody CourseRatingRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        MessageDTO response = contestService.rateCourse(username, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDTO>> getLeaderboard(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        List<LeaderboardEntryDTO> response = contestService.getLeaderboard(id);
        return ResponseEntity.ok(response);
    }
}
