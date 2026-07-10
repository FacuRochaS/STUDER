package facu.studer.controllers;

import facu.studer.security.SecurityUtils;
import facu.studer.services.ContestService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contests")
public class ContestController {

    private final ContestService contestService;
    private final SecurityUtils securityUtils;

    public ContestController(ContestService contestService, SecurityUtils securityUtils) {
        this.contestService = contestService;
        this.securityUtils = securityUtils;
    }


}
