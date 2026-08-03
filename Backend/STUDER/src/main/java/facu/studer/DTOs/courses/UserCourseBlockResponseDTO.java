package facu.studer.DTOs.courses;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserCourseBlockResponseDTO {
    private Long id;
    private Long courseBlockId;
    private Boolean completed;
    private Long duration;
    private Integer attempts;
}
