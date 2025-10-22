package org.pasantia.ahorraya.validation.uservalidations;

import org.pasantia.ahorraya.validation.BusinessException;

/**
 * Exception thrown when an attempt is made to register or use a username that is already in use.
 *
 * <p>This business-level exception indicates a uniqueness violation for the username attribute.</p>
 */
public class DuplicateUsernameException extends BusinessException {

    /**
     * Constructs a new DuplicateUsernameException for the provided username.
     *
     * @param username the username that is already registered
     */
    public DuplicateUsernameException(String username) {
        super("Username '" + username + "' is already registered.");
    }
}
