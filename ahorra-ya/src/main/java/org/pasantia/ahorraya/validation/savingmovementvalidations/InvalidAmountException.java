package org.pasantia.ahorraya.validation.savingmovementvalidations;

import org.pasantia.ahorraya.validation.BusinessException;

/**
 * Exception thrown when a provided amount is invalid for a saving movement.
 *
 * <p>This exception indicates that the monetary amount supplied for a saving
 * movement (credit or debit) does not meet business validation rules, for example
 * it is null, non-positive or otherwise malformed.</p>
 */
public class InvalidAmountException extends BusinessException {

    /**
     * Constructs a new InvalidAmountException with the specified detail message.
     *
     * @param message a human-readable error message in English describing why the amount is invalid
     */
    public InvalidAmountException(String message) {
        super(message);
    }
}
