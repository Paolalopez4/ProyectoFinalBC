package org.pasantia.ahorraya.validation.expensetransactionvalidations;

import org.pasantia.ahorraya.validation.BusinessException;

import java.util.UUID;

/**
 * Exception thrown when an ExpenseTransaction cannot be found.
 *
 * <p>This runtime exception is raised by services or repositories when an
 * operation expects an existing ExpenseTransaction but none matches the
 * provided identifier.</p>
 */
public class ExpenseTransactionNotFoundException extends BusinessException {

    /**
     * Constructs a new ExpenseTransactionNotFoundException for the given transaction id.
     *
     * @param transactionId the UUID of the expense transaction that was not found
     */
    public ExpenseTransactionNotFoundException(UUID transactionId) {
        super("Expense transaction with ID " + transactionId + " was not found.");
    }
}
