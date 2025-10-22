package org.pasantia.ahorraya.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pasantia.ahorraya.model.enums.MovementType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object representing a saving movement.
 *
 * <p>This DTO carries information about a saving movement (credit/debit) and
 * some contextual data about the related saving account and optional expense
 * transaction. Lombok generates constructors, getters and setters.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingMovementDTO {

    /**
     * Unique identifier of the saving movement.
     */
    private UUID id;

    /**
     * Monetary amount of the movement.
     *
     * <p>Represented as BigDecimal to preserve precision.</p>
     */
    private BigDecimal amount;

    /**
     * Type of the movement (e.g. CREDIT or DEBIT).
     */
    private MovementType type;

    /**
     * Short description explaining the purpose of the movement.
     */
    private String description;

    /**
     * Timestamp when the movement was created (ISO-8601 instant in UTC).
     */
    private Instant createdAt;

    /**
     * Identifier of the saving account related to this movement.
     */
    private UUID accountId;

    /**
     * Current balance of the related saving account at the time of retrieval.
     */
    private BigDecimal accountBalance;


    /**
     * Identifier of an associated expense transaction, if any.
     *
     * <p>May be null when the movement is not linked to an expense.</p>
     */
    private UUID expenseTransactionId;
}
