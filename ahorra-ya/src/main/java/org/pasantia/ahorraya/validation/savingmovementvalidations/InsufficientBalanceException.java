package org.pasantia.ahorraya.validation.savingmovementvalidations;

import org.pasantia.ahorraya.validation.BusinessException;

import java.math.BigDecimal;

/**
 * Exception thrown when an attempt is made to withdraw or debit an amount greater than the available account balance.
 *
 * <p>This runtime exception indicates that the requested movement cannot proceed because the
 * account's current balance is insufficient to cover the requested amount.</p>
 */
public class InsufficientBalanceException extends BusinessException {

    /**
     * Constructs a new InsufficientBalanceException with a descriptive English message.
     *
     * @param amount  the attempted withdrawal or debit amount
     * @param balance the current available balance in the account
     */
    public InsufficientBalanceException(BigDecimal amount, BigDecimal balance) {
        super("Insufficient balance: attempted to withdraw " + amount + " but only " + balance + " is available.");
    }
}
