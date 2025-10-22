package org.pasantia.ahorraya.validation.savingaccountvalidations;

import org.pasantia.ahorraya.validation.BusinessException;

import java.util.UUID;

/**
 * Exception thrown when attempting to create a saving account for a user who already has one.
 *
 * <p>This runtime exception signals that the operation would create a duplicate account
 * for the specified user identifier.</p>
 */
public class AccountAlreadyExistsException extends BusinessException {

    /**
     * Constructs a new AccountAlreadyExistsException for the specified user id.
     *
     * @param userId the UUID of the user for whom an account already exists
     */
    public AccountAlreadyExistsException(UUID userId) {
        super("An account for user with ID " + userId + " already exists.");
    }
}
