package org.pasantia.ahorraya.validation.uservalidations;

import org.pasantia.ahorraya.validation.BusinessException;

import java.util.UUID;

/**
 * Exception thrown when a user cannot be found in the system.
 *
 * <p>Used by service and repository layers to indicate that an operation expected
 * an existing User but no matching record was found for the provided identifier.</p>
 */
public class UserNotFoundException extends BusinessException {

    /**
     * Construct a new UserNotFoundException with a custom message.
     *
     * @param message a human-readable detail message (should be in English)
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Construct a new UserNotFoundException for the specified user id.
     *
     * @param userId the UUID of the user that was not found
     */
    public UserNotFoundException(UUID userId) {
        super("User with ID " + userId.toString() + " was not found.");
    }
}
