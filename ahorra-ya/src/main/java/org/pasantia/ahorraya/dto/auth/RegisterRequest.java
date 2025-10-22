package org.pasantia.ahorraya.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object used to register a new user.
 *
 * <p>Contains the basic personal and credential information required to create
 * a user account. Validation annotations enforce presence, format and length
 * constraints.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * Identification number for the user.
     *
     * <p>Must not be blank.</p>
     */
    @NotBlank(message = "Identification number is required")
    private String identificationNumber;

    /**
     * User's first name.
     *
     * <p>Must not be blank.</p>
     */
    @NotBlank(message = "First name is required")
    private String firstName;

    /**
     * User's last name.
     *
     * <p>Must not be blank.</p>
     */
    @NotBlank(message = "Last name is required")
    private String lastName;

    /**
     * User's email address.
     *
     * <p>Must not be blank and must be a valid email address.</p>
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    /**
     * User's phone number.
     *
     * <p>Must not be blank.</p>
     */
    @NotBlank(message = "Phone is required")
    private String phone;

    /**
     * Desired username.
     *
     * <p>Must not be blank and must be between 4 and 50 characters.</p>
     */
    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    /**
     * Account password.
     *
     * <p>Must not be blank and must have at least 6 characters.</p>
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
