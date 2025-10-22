package org.pasantia.ahorraya.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pasantia.ahorraya.model.enums.ExpenseCategory;

import java.math.BigDecimal;

/**
 * Request DTO used to create a new expense.
 *
 * <p>Holds the original expense amount, a short description, the expense category
 * and the merchant name. Validation annotations enforce presence and minimum amount
 * constraints for incoming requests.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateExpenseRequest {

    /**
     * The original amount of the expense.
     *
     * <p>Required. Must be a decimal value greater than or equal to 0.01.</p>
     */
    @NotNull(message = "Original amount is required")
    @DecimalMin(value = "0.01", message = "Original amount must be at least 0.01")
    private BigDecimal originalAmount;

    /**
     * Short description of the expense.
     *
     * <p>Required and should briefly describe the expense.</p>
     */
    @NotBlank(message = "Description is required")
    private String description;

    /**
     * Category of the expense.
     *
     * <p>Required. Should be one of the values defined in {@link ExpenseCategory}.</p>
     */
    @NotNull(message = "Category is required")
    private ExpenseCategory category;

    /**
     * Merchant or vendor where the expense occurred.
     *
     * <p>Required. Provide the merchant name.</p>
     */
    @NotBlank(message = "Merchant is required")
    private String merchant;
}
