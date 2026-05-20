package facu.studer.DTOs.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for token refresh response.
 */
@Data
@Builder
@AllArgsConstructor
public class RefreshResponseDTO {
    /**
     * New JWT access token.
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
     * Creates a refresh response with default token type.
     */
    public static RefreshResponseDTO of(String accessToken, long expiresIn) {
        return RefreshResponseDTO.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}

