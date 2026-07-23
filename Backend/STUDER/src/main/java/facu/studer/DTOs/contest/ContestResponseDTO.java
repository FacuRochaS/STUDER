package facu.studer.DTOs.contest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import facu.studer.DTOs.user.UserPublicResponseDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ContestResponseDTO {
    private Long id;
    private String title;
    private String banner;
    private String description;
    private String theme;
    private String difficulty;
    private JsonNode content;
    private List<String> tags;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime preparationEndDate;
    private LocalDateTime buildingEndDate;
    private LocalDateTime validationEndDate;
    private LocalDateTime endDate;
    private String externalLinks;
    private String bibliography;
    private String learningObjectives;
    private Integer minLevel;
    private Long minReputation;
    private Integer maxParticipants;
    private JsonNode rewards;
    private UserPublicResponseDTO createdBy;
    private Integer participantCount;
    private Integer courseCount;
    private Integer blockCount;
    private LocalDateTime createdDatetime;
}
