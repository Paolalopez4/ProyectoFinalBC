package org.pasantia.ahorraya.validation.savingaccountvalidations;

import org.pasantia.ahorraya.validation.BusinessException;

import java.util.UUID;

/**
 * Exception thrown when a saving account cannot be found.
 *
 * <p>This runtime exception indicates that an operation expected an existing
 * saving account but no matching account was found in the persistence layer.</p>
 */
public class AccountNotFoundException extends BusinessException {

    /**
     * Constructs a new AccountNotFoundException with the specified detail message.
     *
     * @param message the detail message (in English) describing the error
     */
    public AccountNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new AccountNotFoundException for the provided account id.
     *
     * @param accountId the UUID of the account that was not found
     */
    public AccountNotFoundException(UUID accountId) {
        super("Account with ID " + accountId + " was not found.");
    }
}
