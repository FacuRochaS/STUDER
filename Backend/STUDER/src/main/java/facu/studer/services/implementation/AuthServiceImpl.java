package facu.studer.services.implementation;

import facu.studer.DTOs.auth.LoginRequestDTO;
import facu.studer.DTOs.auth.LoginResponseDTO;
import facu.studer.DTOs.auth.RefreshResponseDTO;
import facu.studer.entities.RefreshToken;
import facu.studer.entities.User;
import facu.studer.exceptions.InvalidLoginException;
import facu.studer.exceptions.TokenReuseDetectedException;
import facu.studer.repositories.UserRepository;
import facu.studer.security.JwtUtil;
import facu.studer.security.LoginRateLimiter;
import facu.studer.services.AuthService;
import facu.studer.services.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of AuthService.
 * Handles login, refresh, logout, and current user operations.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginRateLimiter loginRateLimiter;
    private final RefreshTokenService refreshTokenService;

    // Thread-local to pass refresh token to controller for cookie setting
    private final ThreadLocal<String> lastRefreshToken = new ThreadLocal<>();

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            LoginRateLimiter loginRateLimiter,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.loginRateLimiter = loginRateLimiter;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request, String ipAddress, String userAgent) {
        String rateLimitKey = ipAddress + ":" + request.getUsername();

        // Check rate limit
        if (loginRateLimiter.isBlocked(rateLimitKey)) {
            throw new InvalidLoginException("auth.rate_limit_exceeded");
        }

        // Find user
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginRateLimiter.loginFailed(rateLimitKey);
            throw new InvalidLoginException("auth.invalid_credentials");
        }

        // Successful login
        loginRateLimiter.loginSucceeded(rateLimitKey);

        // Generate tokens
        String accessToken = jwtUtil.generateToken(user.getUsername(), List.of());
        String refreshToken = refreshTokenService.generateRefreshToken(user, userAgent, ipAddress);

        // Store refresh token for controller to set cookie
        lastRefreshToken.set(refreshToken);

        return LoginResponseDTO.of(accessToken, jwtUtil.getAccessTokenExpirationSeconds());
    }

    @Override
    @Transactional
    public RefreshResponseDTO refresh(String plainRefreshToken, String ipAddress, String userAgent) {
        // Validate and get refresh token entity
        RefreshToken refreshToken;
        try {
            refreshToken = refreshTokenService.validateRefreshToken(plainRefreshToken);
        } catch (TokenReuseDetectedException e) {
            // Token reuse detected - revoke all user tokens for security
            User user = userRepository.findById(e.getUserId()).orElse(null);
            if (user != null) {
                refreshTokenService.revokeAllUserTokens(user);
            }
            throw e;
        }

        // Rotate refresh token
        String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken, userAgent, ipAddress);
        lastRefreshToken.set(newRefreshToken);

        // Generate new access token
        User user = refreshToken.getUser();
        String accessToken = jwtUtil.generateToken(user.getUsername(), List.of());

        return RefreshResponseDTO.of(accessToken, jwtUtil.getAccessTokenExpirationSeconds());
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }
        lastRefreshToken.remove();
    }

    @Override
    public String getLastGeneratedRefreshToken() {
        String token = lastRefreshToken.get();
        lastRefreshToken.remove();
        return token;
    }
}

