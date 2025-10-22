package org.pasantia.ahorraya.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO used to create a debit (withdrawal) movement on a saving account.
 *
 * <p>Contains the target account identifier, the amount to debit, a short
 * description and an optional linked expense transaction id. Validation
 * annotations enforce required fields and positive amounts.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateDebitRequest {

    /**
     * UUID of the target saving account to be debited.
     *
     * <p>This field is required.</p>
     */
    @NotNull(message = "Account ID is required")
    private UUID accountId;

    /**
     * Amount to debit from the account.
     *
     * <p>This field is required and must be greater than zero.</p>
     */
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    /**
     * Short description for the debit movement.
     *
     * <p>This field is required.</p>
     */
    @NotNull(message = "Description is required")
    private String description;

    /**
     * Optional UUID of an associated expense transaction.
     *
     * <p>If present, links the debit movement to an expense transaction.</p>
     */
    private UUID expenseTransactionId;
}
