package org.pasantia.ahorraya.validation.savingaccountvalidations;

import org.pasantia.ahorraya.validation.BusinessException;

import java.util.UUID;

/**
 * Exception thrown when attempting to operate on a saving account that has already been deleted.
 *
 * <p>This runtime exception indicates that the target saving account for the specified user
 * has been previously removed (soft-deleted) and the requested operation cannot proceed.</p>
 */
public class AccountAlreadyDeletedException extends BusinessException {

    /**
     * Constructs a new AccountAlreadyDeletedException for the given user id.
     *
     * @param userId the UUID of the user whose account was already deleted
     */
    public AccountAlreadyDeletedException(UUID userId) {
        super("Account for user with ID " + userId + " has already been deleted.");
    }
}
