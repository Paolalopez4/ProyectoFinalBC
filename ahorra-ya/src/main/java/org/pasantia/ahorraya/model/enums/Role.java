package org.pasantia.ahorraya.model.enums;

/**
 * Enumeration representing application security roles.
 *
 * <p>Used to assign authorities to principals. Keep values minimal and stable
 * as they may be referenced across security checks and persisted data.</p>
 */
public enum Role {
    /** Regular authenticated user with standard privileges. */
    USER,

    /** Administrator with elevated privileges for management and administration. */
    ADMIN
}
