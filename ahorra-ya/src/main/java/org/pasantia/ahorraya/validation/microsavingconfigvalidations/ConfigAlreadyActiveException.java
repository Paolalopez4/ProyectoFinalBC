package org.pasantia.ahorraya.validation.microsavingconfigvalidations;

import org.pasantia.ahorraya.validation.BusinessException;

import java.util.UUID;

/**
 * Exception thrown when attempting to create or activate a micro-saving configuration
 * for a user that already has an active configuration.
 *
 * <p>Used to indicate that an operation would result in more than one active
 * micro-saving configuration for the same user.</p>
 */
public class ConfigAlreadyActiveException extends BusinessException {

    /**
     * Constructs a new ConfigAlreadyActiveException for the given user id.
     *
     * @param userId the UUID of the user who already has an active configuration
     */
    public ConfigAlreadyActiveException(UUID userId) {
        super("User with ID " + userId + " already has an active micro-saving configuration.");
    }
}
