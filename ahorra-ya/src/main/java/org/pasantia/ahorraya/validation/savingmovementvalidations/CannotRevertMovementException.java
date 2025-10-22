package org.pasantia.ahorraya.validation.savingmovementvalidations;

import org.pasantia.ahorraya.validation.BusinessException;

import java.util.UUID;

/**
 * Exception thrown when a saving movement cannot be reverted.
 *
 * <p>Raised when an operation attempts to revert a saving movement but the
 * movement is not eligible for reversal (for example, due to its status or
 * related business constraints).</p>
 */
public class CannotRevertMovementException extends BusinessException {

    /**
     * Constructs a new CannotRevertMovementException for the given movement id.
     *
     * @param movementId the UUID of the movement that could not be reverted
     */
    public CannotRevertMovementException(UUID movementId) {
        super("Unable to revert saving movement with ID: " + movementId + ".");
    }
}
