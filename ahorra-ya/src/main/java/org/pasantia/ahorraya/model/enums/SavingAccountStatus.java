package org.pasantia.ahorraya.model.enums;

/**
 * Enumeration representing the status of a saving account.
 *
 * <p>Used to indicate whether an account is active and available for operations,
 * inactive (closed or disabled), or blocked due to suspicious activity or policy reasons.</p>
 */
public enum SavingAccountStatus {
    /** Account is active and available for deposits and withdrawals. */
    ACTIVE,

    /** Account is inactive (closed or disabled) and should not accept transactions. */
    INACTIVE,

    /** Account is blocked due to policy or security reasons; transactions are restricted. */
    BLOCKED
}
