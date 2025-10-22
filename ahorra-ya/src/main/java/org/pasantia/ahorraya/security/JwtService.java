package org.pasantia.ahorraya.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsible for creating and validating JSON Web Tokens (JWT).
 *
 * <p>This service generates JWT tokens containing a subject (username) and
 * a role claim, parses tokens to extract claims such as subject and expiration,
 * and validates tokens against Spring Security {@link UserDetails}.</p>
 *
 * <p>Secret and expiration configuration are injected from application properties:
 * {@code jwt.secret} must be a base64url-encoded key; {@code jwt.expiration} is
 * a duration in milliseconds.</p>
 */
@Service
@Slf4j
public class JwtService {

    /**
     * Base64URL-encoded secret key used to sign JWTs.
     *
     * <p>Configured via application property {@code jwt.secret}.</p>
     */
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    /**
     * Token time-to-live in milliseconds.
     *
     * <p>Configured via application property {@code jwt.expiration}.</p>
     */
    @Value("${jwt.expiration}")
    private long JWT_EXPIRATION;

    /**
     * Generates a JWT token for the given authenticated user.
     *
     * @param userDetails user principal information used to build the token subject and claims
     * @return a signed JWT as a compact string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("role", userDetails.getAuthorities()
                .stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("ROLE_USER"));

        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Creates a signed JWT with the provided claims and subject.
     *
     * @param claims  custom claims to include in the token payload
     * @param subject token subject (typically username)
     * @return compact serialized JWT string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username (subject) from the given JWT.
     *
     * @param token the JWT string
     * @return the subject (username) contained in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the given JWT.
     *
     * @param token the JWT string
     * @return expiration {@link Date} of the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the token using the provided resolver function.
     *
     * @param token          the JWT string
     * @param claimsResolver function that retrieves a value from {@link Claims}
     * @param <T>            type of the extracted claim
     * @return extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the token and returns all claims.
     *
     * @param token the JWT string
     * @return {@link Claims} contained in the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks whether the token is expired.
     *
     * @param token the JWT string
     * @return true if the token expiration date is before the current time
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validates the token subject matches the given user and that the token is not expired.
     *
     * @param token       the JWT string
     * @param userDetails user details to validate against the token subject
     * @return true if the token is valid for the provided user, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Decodes the configured secret and returns the signing {@link Key}.
     *
     * @return HMAC signing key derived from {@code SECRET_KEY}
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}