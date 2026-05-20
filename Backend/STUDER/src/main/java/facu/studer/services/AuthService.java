package facu.studer.services;


import facu.studer.DTOs.auth.LoginRequestDTO;
import facu.studer.DTOs.auth.LoginResponseDTO;
import facu.studer.DTOs.auth.RefreshResponseDTO;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Authenticates a user and returns access token.
     *
     * @param request   login credentials
     * @param ipAddress client IP address
     * @param userAgent client user agent
     * @return login response with access token
     */
    LoginResponseDTO login(LoginRequestDTO request, String ipAddress, String userAgent);

    /**
     * Refreshes the access token using refresh token.
     *
     * @param refreshToken the refresh token (from cookie)
     * @param ipAddress    client IP address
     * @param userAgent    client user agent
     * @return new access token
     */
    RefreshResponseDTO refresh(String refreshToken, String ipAddress, String userAgent);

    /**
     * Logs out the user by revoking the refresh token.
     *
     * @param refreshToken the refresh token (from cookie)
     */
    void logout(String refreshToken);

    /**
     * Gets the plain refresh token for cookie.
     * Called after successful login.
     *
     * @return the last generated refresh token
     */
    String getLastGeneratedRefreshToken();
}

