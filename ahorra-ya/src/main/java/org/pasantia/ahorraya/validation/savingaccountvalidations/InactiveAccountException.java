package org.pasantia.ahorraya.validation.savingaccountvalidations;

import org.pasantia.ahorraya.validation.BusinessException;

/**
 * Exception thrown when an operation is attempted on a saving account that is not active.
 *
 * <p>Used to indicate that the target saving account is inactive and therefore
 * cannot accept operations such as deposits or withdrawals.</p>
 */
public class InactiveAccountException extends BusinessException {

    /**
     * Constructs a new InactiveAccountException for the specified account number.
     *
     * @param accountNumber the account number of the inactive saving account
     */
    public InactiveAccountException(String accountNumber) {
        super("Saving account with number " + accountNumber + " is inactive.");
    }
}
