package org.pasantia.ahorraya.validation.uservalidations;

import org.pasantia.ahorraya.validation.BusinessException;

/**
 * Exception thrown when an attempt is made to register an identification value
 * that is already present in the system.
 *
 * <p>Used to signal a uniqueness violation for the user's identification number.</p>
 */
public class DuplicateIdentificationException extends BusinessException {

    /**
     * Constructs a new DuplicateIdentificationException for the provided identification value.
     *
     * @param identification the identification value that is duplicated
     */
    public DuplicateIdentificationException(String identification) {
        super("Identification number '" + identification + "' is already registered.");
    }
}
