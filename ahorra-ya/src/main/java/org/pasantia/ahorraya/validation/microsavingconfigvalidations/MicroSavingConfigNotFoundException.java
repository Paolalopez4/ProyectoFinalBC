package org.pasantia.ahorraya.validation.microsavingconfigvalidations;

import java.util.UUID;

/**
 * Exception thrown when a micro-saving configuration cannot be found.
 *
 * <p>This runtime exception indicates that an operation expected an existing
 * MicroSavingConfig but no configuration matched the provided identifier.</p>
 */
public class MicroSavingConfigNotFoundException extends RuntimeException {

    /**
     * Constructs a new MicroSavingConfigNotFoundException for the given configuration id.
     *
     * @param configId the UUID of the micro-saving configuration that was not found
     */
    public MicroSavingConfigNotFoundException(UUID configId) {
        super("Micro-saving configuration with ID " + configId + " was not found.");
    }
}
