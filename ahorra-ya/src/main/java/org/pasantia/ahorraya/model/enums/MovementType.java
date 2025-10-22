package org.pasantia.ahorraya.model.enums;

/**
 * Enumeration representing the type of a saving movement.
 *
 * <p>Used to distinguish between incoming (CREDIT) and outgoing (DEBIT)
 * movements on a saving account. A case-insensitive factory method is
 * provided to convert incoming string values into the corresponding enum.</p>
 */
public enum MovementType {

    /** Movement that increases the account balance (deposit). */
    CREDIT,

    /** Movement that decreases the account balance (withdrawal). */
    DEBIT;
}
