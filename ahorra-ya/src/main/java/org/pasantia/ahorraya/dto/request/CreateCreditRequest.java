package org.pasantia.ahorraya.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO used to create a credit (deposit) movement on a saving account.
 *
 * <p>Contains the target account identifier, the amount to credit, a short
 * description and an optional linked expense transaction id. Lombok generates
 * boilerplate constructors, getters and setters.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCreditRequest {

    /**
     * UUID of the target saving account to be credited.
     *
     * <p>This field is required.</p>
     */
    @NotNull(message = "Account ID is required")
    private UUID accountId;

    /**
     * Amount to credit to the account.
     *
     * <p>This field is required and must be at least 0.01.</p>
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    /**
     * Short description for the credit movement.
     *
     * <p>This field is required.</p>
     */
    @NotBlank(message = "Description is required")
    private String description;

    /**
     * Optional UUID of an associated expense transaction.
     *
     * <p>If present, links the credit movement to an expense transaction.</p>
     */
    private UUID expenseTransactionId;
}
