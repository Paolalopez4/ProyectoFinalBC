package org.pasantia.ahorraya.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Response DTO returned after a successful authentication.
 *
 * <p>Contains the issued JWT token, token type, basic user identification,
 * assigned role and token expiration information.</p>
 */
public class AuthResponse {

    /**
     * The JWT token string that should be used for authenticated requests.
     */
    private String token;

    /**
     * The token type (usually "Bearer").
     */
    private String type = "Bearer";

    /**
     * The username associated with the token (or login identifier).
     */
    private String username;

    /**
     * The email address associated with the authenticated principal.
     */
    private String email;

    /**
     * The role assigned to the authenticated principal (e.g. "USER", "ADMIN").
     */
    private String role;

    /**
     * Time in milliseconds until the token expires (relative TTL).
     */
    private Long expiresIn;
}
