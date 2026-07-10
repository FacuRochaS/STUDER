package facu.studer.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import facu.studer.security.SecurityUtils;
import facu.studer.services.ContestService;
import jakarta.persistence.Column;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {
    private final SecurityUtils securityUtils;
    private final ContestService contestService;

    public CourseController(SecurityUtils securityUtils, ContestService contestService) {
        this.securityUtils = securityUtils;
        this.contestService = contestService;
    }


}
