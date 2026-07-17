package facu.studer.controllers;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.contest.ContestCreateRequestDTO;
import facu.studer.DTOs.contest.ContestResponseDTO;
import facu.studer.security.SecurityUtils;
import facu.studer.services.ContestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final ContestService contestService;
    private final SecurityUtils securityUtils;

    public AdminController(ContestService contestService, SecurityUtils securityUtils) {
        this.contestService = contestService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/contests")
    public ResponseEntity<ContestResponseDTO> createContest(
            @Valid @RequestBody ContestCreateRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        ContestResponseDTO response = contestService.createContest(username, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/contests/{id}/finish")
    public ResponseEntity<MessageDTO> finishContest(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        MessageDTO response = contestService.finishContest(username, id);
        return ResponseEntity.ok(response);
    }
}
