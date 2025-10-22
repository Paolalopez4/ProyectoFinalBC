package org.pasantia.ahorraya.validation.savingmovementvalidations;

import org.pasantia.ahorraya.validation.BusinessException;

/**
 * Exception thrown when a saving movement is in an unexpected status for the requested operation.
 *
 * <p>This exception indicates that the current status of a saving movement does not match
 * the required/expected status for the attempted action.</p>
 */
public class InvalidMovementStatusException extends BusinessException {

    /**
     * Constructs a new InvalidMovementStatusException with a descriptive English message.
     *
     * @param currentStatus  the current status of the movement
     * @param requiredStatus the required or expected status for the operation
     */
    public InvalidMovementStatusException(String currentStatus, String requiredStatus) {
        super("Invalid movement status: current status is '" + currentStatus + "', expected '" + requiredStatus + "'.");
    }
}
