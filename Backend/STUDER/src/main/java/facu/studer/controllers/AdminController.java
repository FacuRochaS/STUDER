package facu.studer.controllers;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.contest.*;
import facu.studer.security.SecurityUtils;
import facu.studer.services.AdminService;
import facu.studer.services.ContestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final ContestService contestService;
    private final AdminService adminService;
    private final SecurityUtils securityUtils;

    public AdminController(ContestService cs, AdminService as, SecurityUtils su) { this.contestService = cs; this.adminService = as; this.securityUtils = su; }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Integer limit) {
        securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(adminService.getDashboard(startDate, endDate, date, limit));
    }

    @GetMapping("/dashboard/users")
    public ResponseEntity<Map<String, Object>> usersDashboard(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer limit) {
        securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(adminService.getUsersDashboard(startDate, endDate, limit));
    }

    @GetMapping("/dashboard/blocks")
    public ResponseEntity<Map<String, Object>> blocksDashboard(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer limit) {
        securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(adminService.getBlocksDashboard(startDate, endDate, limit));
    }

    @GetMapping("/dashboard/courses")
    public ResponseEntity<Map<String, Object>> coursesDashboard(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer limit) {
        securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(adminService.getCoursesDashboard(startDate, endDate, limit));
    }

    @GetMapping("/dashboard/feed")
    public ResponseEntity<Map<String, Object>> feedDashboard(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer limit) {
        securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(adminService.getFeedDashboard(startDate, endDate, limit));
    }

    @GetMapping("/dashboard/discussions")
    public ResponseEntity<Map<String, Object>> discussionsDashboard(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer limit) {
        securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(adminService.getDiscussionsDashboard(startDate, endDate, limit));
    }

    @GetMapping("/dashboard/contests")
    public ResponseEntity<Map<String, Object>> contestsDashboard(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer limit) {
        securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(adminService.getContestsDashboard(startDate, endDate, limit));
    }

    @GetMapping("/dashboard/tags")
    public ResponseEntity<Map<String, Object>> tagsDashboard(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer limit) {
        securityUtils.requireCurrentUsername();
        return ResponseEntity.ok(adminService.getTagsDashboard(startDate, endDate, limit));
    }

    @PostMapping("/contests") public ResponseEntity<ContestResponseDTO> create(@Valid @RequestBody ContestCreateRequestDTO r) { return ResponseEntity.ok(contestService.createContest(securityUtils.requireCurrentUsername(), r)); }
    @PutMapping("/contests/{id}") public ResponseEntity<ContestResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ContestCreateRequestDTO r) { return ResponseEntity.ok(contestService.updateContest(securityUtils.requireCurrentUsername(), id, r)); }
    @DeleteMapping("/contests/{id}") public ResponseEntity<MessageDTO> delete(@PathVariable Long id) { return ResponseEntity.ok(contestService.deleteContest(securityUtils.requireCurrentUsername(), id)); }
    @PatchMapping("/contests/{id}/status") public ResponseEntity<MessageDTO> status(@PathVariable Long id, @RequestBody StatusChangeRequest r) { return ResponseEntity.ok(contestService.changeContestStatus(securityUtils.requireCurrentUsername(), id, r.getStatus())); }
    @PostMapping("/contests/{id}/finish") public ResponseEntity<MessageDTO> finish(@PathVariable Long id) { return ResponseEntity.ok(contestService.finishContest(securityUtils.requireCurrentUsername(), id)); }
}
