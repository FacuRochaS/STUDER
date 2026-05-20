package facu.studer.DTOs.user;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.LocalDate;

/**
 * Data Transfer Object for creating a new User entry.
 * Contains all required fields for User creation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserCreateRequestDTO {

    /** First name of the user */
    @NotBlank(message = "{user.first_name.required}")
    private String firstName;

    /** Last name of the user */
    @NotBlank(message = "{user.last_name.required}")
    private String lastName;

    /** Birthdate of the user */
    @NotNull(message = "{user.birthdate.required}")
    private LocalDate birthDate;


    /**
     * Minimum number of characters for the code.
     */
    public static final int EMAIL_MIN_LENGTH = 5;

    /**
     * Maximum number of characters for the code.
     */
    public static final int EMAIL_MAX_LENGTH = 50;

    /**
     * Registered email.
     */
    @NotBlank(message = "{user.email.required}")
    @Email(message = "{user.email.invalid}")
    @Size(min = EMAIL_MIN_LENGTH, max = EMAIL_MAX_LENGTH, message = "{user.email.size}")
    private String email;


    /**
     * Minimum number of characters for the Username.
     */
    public static final int USERNAME_MIN_LENGTH = 6;

    /**
     * Maximum number of characters for the Username.
     */
    public static final int USERNAME_MAX_LENGTH = 50;

    /**
     * Unique username.
     */
    @NotBlank(message = "{user.username.required}")
    @Size(min = USERNAME_MIN_LENGTH, max = USERNAME_MAX_LENGTH, message = "{user.username.size}")
    private String username;


    /**
     * Minimum number of characters for the password.
     */
    public static final int PASSWORD_MIN_LENGTH = 8;
    /**
     * Password.
     */
    @NotBlank(message = "{user.password.required}")
    @Size(min = PASSWORD_MIN_LENGTH,  message = "{user.password.size}")
    private String password;

}
