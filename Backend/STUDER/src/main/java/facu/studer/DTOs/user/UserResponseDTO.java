package facu.studer.DTOs.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserResponseDTO {

    /**
     * User ID.
     */
    private Long id;

    /**
     * Username.
     */
    private String username;

    /**
     * Email.
     */
    private String email;

    /**
     * First name.
     */
    private String firstName;

    /**
     * Last name.
     */
    private String lastName;
}
