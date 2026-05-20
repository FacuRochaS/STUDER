package facu.studer.controllers;

import facu.studer.DTOs.auth.LoginRequestDTO;
import facu.studer.DTOs.auth.LoginResponseDTO;
import facu.studer.DTOs.auth.RefreshResponseDTO;
import facu.studer.security.SecurityUtils;
import facu.studer.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations.
 * Handles login, refresh, logout, and current user endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityUtils securityUtils;

    public AuthController(AuthService authService, SecurityUtils securityUtils) {
        this.authService = authService;
        this.securityUtils = securityUtils;
    }

    /**
     * Authenticates a user and returns an access token.
     * Sets refresh token in HttpOnly cookie.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        LoginResponseDTO response = authService.login(
                request,
                securityUtils.getClientIp(httpRequest),
                securityUtils.getUserAgent(httpRequest)
        );

        String refreshToken = authService.getLastGeneratedRefreshToken();
        if (refreshToken != null) {
            securityUtils.addRefreshTokenCookie(httpResponse, refreshToken);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes the access token using the refresh token from cookie.
     * Rotates the refresh token (new cookie set).
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseDTO> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String refreshToken = securityUtils.extractRefreshTokenFromCookie(httpRequest);
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        RefreshResponseDTO response = authService.refresh(
                refreshToken,
                securityUtils.getClientIp(httpRequest),
                securityUtils.getUserAgent(httpRequest)
        );

        String newRefreshToken = authService.getLastGeneratedRefreshToken();
        if (newRefreshToken != null) {
            securityUtils.addRefreshTokenCookie(httpResponse, newRefreshToken);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Logs out the user by revoking the refresh token.
     * Clears the refresh token cookie.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String refreshToken = securityUtils.extractRefreshTokenFromCookie(httpRequest);
        authService.logout(refreshToken);
        securityUtils.clearRefreshTokenCookie(httpResponse);

        return ResponseEntity.noContent().build();
    }


}

