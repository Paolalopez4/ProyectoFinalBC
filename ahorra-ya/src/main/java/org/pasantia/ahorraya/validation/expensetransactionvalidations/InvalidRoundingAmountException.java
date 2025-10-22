package org.pasantia.ahorraya.validation.expensetransactionvalidations;

import org.pasantia.ahorraya.validation.BusinessException;

import java.math.BigDecimal;

/**
 * Exception thrown when an expense amount cannot be rounded according to the configured rules.
 *
 * <p>This exception indicates that the provided original amount cannot be converted/rounded
 * to the expected rounded value under the current micro-saving or rounding configuration.</p>
 */
public class InvalidRoundingAmountException extends BusinessException {

    /**
     * Constructs a new InvalidRoundingAmountException with a descriptive English message.
     *
     * @param amount  the original amount that could not be rounded
     * @param rounded the attempted rounded value
     */
    public InvalidRoundingAmountException(BigDecimal amount, BigDecimal rounded) {
        super("Amount " + amount + " cannot be rounded to " + rounded);
    }
}
