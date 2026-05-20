package facu.studer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {
    /**
     * Secret key for signing the JWT.
     */
    private final Key key;
    /**
     * JWT access token expiration time in milliseconds.
     * Default: 15 minutes (900000ms)
     */
    private final long accessTokenExpirationMs;

    /**
     * Constructor that receives the secret key from configuration.
     * @param secret secret key for signing the JWT
     * @param accessTokenExpirationMs access token expiration in milliseconds
     */
    public JwtUtil(
            @Value("${JWT_SECRET}") String secret,
            @Value("${app.security.access-token-expiration-ms:900000}") long accessTokenExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    /**
     * Generates a JWT access token for the given user.
     * @param username username
     * @return generated JWT token
     */
    public String generateToken(String username) {
        return generateToken(username, List.of());
    }

    /**
     * Generates a JWT access token with roles.
     * @param username username
     * @param roles user roles
     * @return generated JWT token
     */
    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString()) // jti - unique token id
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Gets the access token expiration time in seconds.
     * @return expiration time in seconds
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }

    /**
     * Extracts the username from the JWT token.
     * @param token the JWT token
     * @return extracted username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the JWT token.
     * @param token the JWT token
     * @return extracted expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a claim from the JWT token.
     * @param token the JWT token
     * @param claimsResolver function to resolve the claim
     * @param <T> type of the claim
     * @return extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    /**
     * Checks if the JWT token is valid for the given user.
     * @param token the JWT token
     * @param username username
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the token ID (jti) from the JWT token.
     * @param token the JWT token
     * @return extracted token ID
     */
    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    /**
     * Extracts roles from the JWT token.
     * @param token the JWT token
     * @return list of roles
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }
}
