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
public class UserReputationDTO {
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private long points;
}
