package org.pasantia.ahorraya.model.enums;

/**
 * Enumeration representing the status of a user account.
 *
 * <p>Indicates whether a user account is active, inactive (deactivated), or
 * suspended due to policy or security reasons.</p>
 */
public enum UserStatus {

    /** Account is active and allowed to perform normal operations. */
    ACTIVE,

    /** Account is inactive or deactivated and should not be used for operations. */
    INACTIVE,

    /** Account is suspended due to policy, fraud or security concerns. */
    SUSPENDED
}
