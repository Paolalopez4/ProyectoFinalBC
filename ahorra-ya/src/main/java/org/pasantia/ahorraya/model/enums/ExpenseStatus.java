package org.pasantia.ahorraya.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

/**
 * Enumeration that represents the processing status of an expense transaction.
 *
 * <p>Used to indicate whether an expense is awaiting processing, has been processed
 * (and any saving logic applied), or was rejected.</p>
 */
public enum ExpenseStatus {
    /** Expense is awaiting processing. */
    PENDING,

    /** Expense has been processed and any associated saving logic was applied. */
    PROCESSED,

    /** Expense was rejected or considered invalid. */
    REJECTED;
}
