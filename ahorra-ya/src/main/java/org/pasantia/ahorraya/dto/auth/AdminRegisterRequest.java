package org.pasantia.ahorraya.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO used to register a new administrator.
 *
 * <p>Contains the administrator's email and password. Validation annotations
 * enforce presence, format and minimum password length requirements.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRegisterRequest {
    /**
     * Administrator email address.
     *
     * <p>Must not be blank and must conform to a valid email address format.</p>
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    /**
     * Administrator password.
     *
     * <p>Must not be blank and must have at least 8 characters.</p>
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
