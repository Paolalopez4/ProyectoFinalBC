package org.pasantia.ahorraya.validation.adminvalidations;

import java.util.UUID;

/**
 * Exception thrown when an administrator record cannot be found.
 *
 * <p>Used to signal that an Admin/Administrator entity referenced by id or other
 * identifier does not exist in the persistence layer.</p>
 */
public class AdminNotFoundException extends RuntimeException {

    /**
     * Creates a new AdminNotFoundException with the specified detail message.
     *
     * @param message the detail message (in English) describing the error
     */
    public AdminNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new AdminNotFoundException for the provided administrator id.
     *
     * @param adminId the UUID of the administrator that was not found
     */
    public AdminNotFoundException(UUID adminId) {
        super("Administrator with ID " + adminId + " was not found.");
    }
}
