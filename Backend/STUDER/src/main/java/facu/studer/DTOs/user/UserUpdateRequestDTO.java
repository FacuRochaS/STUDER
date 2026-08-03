package facu.studer.DTOs.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Data Transfer Object for updating a User.
 * Contains all required fields for User update.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserUpdateRequestDTO {

    /**
     * Minimum number of characters for the code.
     */
    public static final int EMAIL_MIN_LENGTH = 6;

    /**
     * Maximum number of characters for the code.
     */
    public static final int EMAIL_MAX_LENGTH = 30;


    /**
     * Minimum number of characters for the password.
     */
    public static final int PASSWORD_MIN_LENGTH = 8;
    /**
     * Password.
     */
    @Size(min = PASSWORD_MIN_LENGTH,  message = "{user.password.size}")
    private String password;
}
