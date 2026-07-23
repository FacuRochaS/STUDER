package facu.studer.DTOs.contest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ContestCreateRequestDTO {

    @NotBlank
    private String title;

    private String banner;
    private String description;
    private String theme;
    private String difficulty;
    private JsonNode content;
    private List<String> tags;
    private String externalLinks;
    private String bibliography;
    private String learningObjectives;
    private Integer minLevel;
    private Long minReputation;
    private Integer maxParticipants;
    private JsonNode rewards;

    @NotNull
    private LocalDateTime startDate;

    private LocalDateTime preparationEndDate;
    private LocalDateTime buildingEndDate;
    private LocalDateTime validationEndDate;

    @NotNull
    private LocalDateTime endDate;
}
