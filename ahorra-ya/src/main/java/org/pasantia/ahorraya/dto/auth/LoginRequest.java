package org.pasantia.ahorraya.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for authentication requests.
 *
 * <p>Contains the credentials required to authenticate a user: either a username
 * or email and the corresponding password. Validation annotations ensure required
 * fields are provided.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Username or email used to authenticate.
     *
     * <p>Accepts either the user's username or their email address. This field
     * must not be blank.</p>
     */
    @NotBlank(message = "Username or email is required")
    private String username;

    /**
     * Password for authentication.
     *
     * <p>This field must not be blank.</p>
     */
    @NotBlank(message = "Password is required")
    private String password;
}
