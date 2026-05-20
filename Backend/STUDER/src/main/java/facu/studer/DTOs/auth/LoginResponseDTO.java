package facu.studer.DTOs.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for login response containing access token.
 */
@Data
@Builder
@AllArgsConstructor
public class LoginResponseDTO {
    /**
     * JWT access token for authenticated user.
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * Token type (always "Bearer").
     */
    @JsonProperty("token_type")
    private String tokenType;

    /**
     * Access token expiration time in seconds.
     */
    @JsonProperty("expires_in")
    private long expiresIn;

    /**
     * Creates a login response with default token type.
     */
    public static LoginResponseDTO of(String accessToken, long expiresIn) {
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}

