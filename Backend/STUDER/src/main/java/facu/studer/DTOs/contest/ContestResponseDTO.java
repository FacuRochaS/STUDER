package facu.studer.DTOs.contest;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
    private String description;
    private String content;
    private List<String> tags;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime preparationEndDate;
    private LocalDateTime validationEndDate;
    private Integer preparationDurationHours;
    private Integer validationDurationHours;
    private Long minPoints;
    private Integer participantCount;
    private Integer courseCount;
    private Integer blockCount;
    private LocalDateTime createdDatetime;
}
