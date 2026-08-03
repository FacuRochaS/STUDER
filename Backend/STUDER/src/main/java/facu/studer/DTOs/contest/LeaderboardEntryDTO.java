package facu.studer.DTOs.contest;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LeaderboardEntryDTO {
    private Long courseId;
    private String courseName;
    private Double averageRating;
    private Long ratingCount;
    private Long likeCount;
    private Double score;
}
