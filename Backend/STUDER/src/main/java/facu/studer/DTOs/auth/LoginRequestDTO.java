package facu.studer.DTOs.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for login request.
 */
@Data
public class LoginRequestDTO {
    /**
     * Username for login.
     */
    public static final int USERNAME_MIN_LENGTH = 6;
    /**
     * Maximum number of characters for the username.
     */
    public static final int USERNAME_MAX_LENGTH = 50;
    /**
     * Password for login.
     */
    public static final int PASSWORD_MIN_LENGTH = 8;
    /**
     * Maximum number of characters for the password.
     */
    public static final int PASSWORD_MAX_LENGTH = 100;

    /**
     * Username for login.
     */
    @NotBlank(message = "{user.username.required}")
    @Size(min = USERNAME_MIN_LENGTH, max = USERNAME_MAX_LENGTH, message = "{user.username.size}")
    private String username;

    /**
     * Password for login.
     */
    @NotBlank(message = "{user.password.required}")
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH, message = "{user.password.size}")
    private String password;
}

