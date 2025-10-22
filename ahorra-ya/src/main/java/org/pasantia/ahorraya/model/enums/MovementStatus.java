package org.pasantia.ahorraya.model.enums;

/**
 * Enumeration representing the processing state of a saving movement.
 *
 * <p>Used to indicate the lifecycle or outcome of a movement (for example:
 * pending, processed, reverted, completed or failed). A case-insensitive
 * factory method is provided to convert incoming string values into the
 * corresponding enum value.</p>
 */
public enum MovementStatus {
    /** Movement has been processed successfully and any business logic applied. */
    PROCESSED,

    /** Movement has been reverted (rolled back) after being processed. */
    REVERTED,

    /** Movement has completed its lifecycle (finalized). */
    COMPLETED,

    /** Movement processing failed due to an error. */
    FAILED,

    /** Movement is pending and awaiting processing. */
    PENDING;
}
