package org.pasantia.ahorraya.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pasantia.ahorraya.model.enums.UserStatus;

/**
 * Data Transfer Object used to create a new user.
 *
 * <p>Contains personal information, credentials and an optional account status.
 * Validation annotations enforce presence, format and length constraints on incoming requests.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    /**
     * Identification number for the user.
     *
     * <p>Required. Must match the pattern of upper-case letters and digits with length between 5 and 20.</p>
     */
    @NotBlank(message = "Identification number is required")
    @Pattern(regexp = "^[A-Z0-9]{5,20}$", message = "Invalid identification format")
    private String identificationNumber;

    /**
     * User's first name.
     *
     * <p>Required. Must be between 2 and 50 characters.</p>
     */
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    /**
     * User's last name.
     *
     * <p>Required. Must be between 2 and 50 characters.</p>
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    /**
     * User's email address.
     *
     * <p>Required. Must be a valid email format.</p>
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * User's phone number in E.164 format.
     *
     * <p>Required. Must match an international phone number format.</p>
     */
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone format")
    private String phone;

    /**
     * Desired username.
     *
     * <p>Required. Must be between 3 and 50 characters.</p>
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * Account password.
     *
     * <p>Required. Must have at least 8 characters.</p>
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /**
     * Optional user status. If not provided, defaults to ACTIVE.
     */
    private UserStatus status;
}