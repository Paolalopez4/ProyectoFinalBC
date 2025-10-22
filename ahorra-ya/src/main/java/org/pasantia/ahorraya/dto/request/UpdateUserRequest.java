package org.pasantia.ahorraya.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pasantia.ahorraya.model.enums.UserStatus;

/**
 * DTO used to update an existing user's mutable fields.
 *
 * <p>All fields are optional; provided values will be validated. Use this DTO
 * to partially update user attributes such as name, contact info, username
 * and account status.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    /**
     * User's first name.
     *
     * <p>Optional. If provided, must be between 2 and 50 characters.</p>
     */
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    /**
     * User's last name.
     *
     * <p>Optional. If provided, must be between 2 and 50 characters.</p>
     */
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    /**
     * User's email address.
     *
     * <p>Optional. If provided, must be a valid email format.</p>
     */
    @Email(message = "Invalid email format")
    private String email;

    /**
     * User's phone number in E.164 format.
     *
     * <p>Optional. If provided, must match an international phone number pattern.</p>
     */
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone format")
    private String phone;

    /**
     * Desired username.
     *
     * <p>Optional. If provided, must be between 3 and 50 characters.</p>
     */
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * Optional user status to update.
     *
     * <p>When provided, will replace the user's current status.</p>
     */
    private UserStatus status;
}
