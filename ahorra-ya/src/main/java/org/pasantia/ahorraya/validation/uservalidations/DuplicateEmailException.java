package org.pasantia.ahorraya.validation.uservalidations;

import org.pasantia.ahorraya.validation.BusinessException;

/**
 * Exception thrown when an attempt is made to register or use an email address
 * that is already associated with an existing user in the system.
 *
 * <p>This is a domain-level business exception indicating a uniqueness violation
 * for the email attribute.</p>
 */
public class DuplicateEmailException extends BusinessException {

    /**
     * Constructs a new DuplicateEmailException for the provided email address.
     *
     * @param email the email address that is already registered
     */
    public DuplicateEmailException(String email) {
        super("Email '" + email + "' is already registered.");
    }
}
