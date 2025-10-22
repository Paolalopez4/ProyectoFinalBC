package org.pasantia.ahorraya.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.pasantia.ahorraya.model.enums.MovementStatus;
import org.pasantia.ahorraya.model.enums.MovementType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a movement (credit or debit) applied to a saving account.
 * A SavingMovement links a SavingAccount with an ExpenseTransaction and the User performing the operation.
 * It tracks amounts before/after the movement, its date, current status and an embedded Audit object.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "saving_movements")
@Entity
public class SavingMovement {
    /**
     * Primary identifier for the saving movement (UUID).
     * Never null after persistence.
     */
    @Getter
    @NotNull
    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * The saving account affected by this movement.
     * This association is required and not updatable after creation.
     */
    @Getter
    @Setter
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saving_account_id", nullable = false, updatable = false)
    @JsonBackReference
    private SavingAccount savingAccount;

    /**
     * The expense transaction that originated this saving movement.
     * Required and immutable once set.
     */
    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_transaction_id", nullable = true, updatable = false)
    private ExpenseTransaction expenseTransaction;

    /**
     * The user who performed or created this movement.
     * Required and immutable once set.
     */
    @Getter
    @Setter
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    /**
     * Amount to be saved (positive value).
     * Stored with two decimal precision.
     */
    @Getter
    @Positive
    @NotNull
    @Column(name = "savings_amount", nullable = false, updatable = false)
    private BigDecimal savingsAmount;

    /**
     * Type of movement: CREDIT or DEBIT.
     * Stored as string in the database and immutable after creation.
     */
    @Getter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type_id", nullable = false, updatable = false)
    private MovementType movementType;

    /**
     * Brief description for the movement.
     */
    @Getter
    @Setter
    @NotNull
    @Column(nullable = false)
    private String description;

    /**
     * Account balance before applying this movement.
     * Maintained with two decimal precision.
     */
    @Getter
    @NotNull
    @Column(name = "previous_balance", nullable = false)
    private BigDecimal previousBalance;

    /**
     * Account balance after applying this movement.
     * Maintained with two decimal precision.
     */
    @Getter
    @NotNull
    @Column(name = "new_balance", nullable = false)
    private BigDecimal newBalance;

    /**
     * Timestamp when the movement occurred or was last updated.
     */
    @Getter
    @NotNull
    @Column(name = "movement_date", nullable = false)
    private Instant movementDate;

    /**
     * Current status of the movement (PENDING, COMPLETED, REVERTED, ...).
     */
    @Getter
    @Setter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_status_id", nullable = false)
    private MovementStatus movementStatus;

    /**
     * Embedded audit information (created/updated timestamps and user info).
     */
    @Getter
    @NotNull
    @Embedded
    private final Audit audit = new Audit();

    /**
     * Create a new SavingMovement.
     *
     * @param savingAccount     the saving account affected by this movement (required)
     * @param expenseTransaction the expense transaction that originated this movement (required)
     * @param user              the user performing the movement (required)
     * @param savingsAmount     the positive amount to be saved (must be > 0)
     * @param movementType      the type of movement; if null defaults to {@link MovementType#CREDIT}
     * @param description       a short description for the movement
     * @throws IllegalArgumentException if savingsAmount is null or non-positive
     */
    public SavingMovement(SavingAccount savingAccount, ExpenseTransaction expenseTransaction, User user,
                          BigDecimal savingsAmount, MovementType movementType, String description) {
        if (savingsAmount == null || savingsAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Savings amount must be positive");
        }

        this.savingAccount = savingAccount;
        this.expenseTransaction = expenseTransaction;
        this.user = user;
        this.savingsAmount = savingsAmount;
        this.movementType = movementType != null ? movementType : MovementType.CREDIT;
        this.description = description;
        this.movementStatus = MovementStatus.PENDING;
        this.previousBalance = BigDecimal.ZERO;
        this.newBalance = BigDecimal.ZERO;
        this.movementDate = Instant.now();
    }

    /**
     * Apply this movement to the associated saving account.
     * For CREDIT movements the account is credited by {@link #savingsAmount}.
     * For DEBIT movements the account is debited by {@link #savingsAmount}.
     * This method updates previousBalance, newBalance, movementDate and sets status to COMPLETED.
     *
     * @throws IllegalStateException    if the saving account is missing or the movement is not PENDING
     * @throws IllegalArgumentException if movementType is unsupported
     */
    public void applyMovement() {
        validateCanApply();

        BigDecimal current = normalize(this.savingAccount.getBalance());
        this.previousBalance = current;

        if (this.movementType == MovementType.CREDIT) {
            this.savingAccount.credit(this.savingsAmount);
        } else if (this.movementType == MovementType.DEBIT) {
            this.savingAccount.debit(this.savingsAmount);
        } else {
            throw new IllegalArgumentException("Unsupported movement type: " + this.movementType);
        }

        this.newBalance = normalize(this.savingAccount.getBalance());
        this.movementDate = Instant.now();
        this.movementStatus = MovementStatus.COMPLETED;
    }

    /**
     * Revert a previously applied movement.
     * If movement was CREDIT, the account is debited by the same amount.
     * If movement was DEBIT, the account is credited by the same amount.
     * Updates balances, movementDate and sets status to REVERTED.
     *
     * @throws IllegalStateException    if the saving account is missing or the movement is not COMPLETED
     * @throws IllegalArgumentException if movementType is unsupported
     */
    public void revertMovement() {
        validateCanRevert();

        BigDecimal current = normalize(this.savingAccount.getBalance());
        this.previousBalance = current;

        if (this.movementType == MovementType.CREDIT) {
            this.savingAccount.debit(this.savingsAmount);
        } else if (this.movementType == MovementType.DEBIT) {
            this.savingAccount.credit(this.savingsAmount);
        } else {
            throw new IllegalArgumentException("Unsupported movement type: " + this.movementType);
        }

        this.newBalance = normalize(this.savingAccount.getBalance());
        this.movementDate = Instant.now();
        this.movementStatus = MovementStatus.REVERTED;
    }

    /**
     * Validate that the movement can be applied.
     *
     * @throws IllegalStateException if savingAccount is null or status is not PENDING
     */
    private void validateCanApply() {
        if (this.savingAccount == null) {
            throw new IllegalStateException("Saving account is required to apply movement");
        }
        if (this.movementStatus != MovementStatus.PENDING) {
            throw new IllegalStateException("Only PENDING movements can be applied. Current status: " + this.movementStatus);
        }
    }

    /**
     * Validate that the movement can be reverted.
     *
     * @throws IllegalStateException if savingAccount is null or status is not COMPLETED
     */
    private void validateCanRevert() {
        if (this.savingAccount == null) {
            throw new IllegalStateException("Saving account is required to revert movement");
        }
        if (this.movementStatus != MovementStatus.COMPLETED) {
            throw new IllegalStateException("Only COMPLETED movements can be reverted. Current status: " + this.movementStatus);
        }
    }

    /**
     * Ensure numeric fields are normalized to two decimal places and non-null.
     * Used before persisting/updating values.
     *
     */
    @PrePersist
    private void prePersist() {
        this.savingsAmount = normalize(this.savingsAmount);
        this.previousBalance = normalize(this.previousBalance);
        this.newBalance = normalize(this.newBalance);
        if (this.movementDate == null) {
            this.movementDate = Instant.now();
        }
        this.audit.prePersist();
    }

    /**
     * Normalize numeric fields and update audit timestamp before update.
     */
    @PreUpdate
    private void preUpdate() {
        this.savingsAmount = normalize(this.savingsAmount);
        this.previousBalance = normalize(this.previousBalance);
        this.newBalance = normalize(this.newBalance);
        this.audit.updateTimestamp();
    }

    /**
     * Normalize an amount to two decimal places using HALF_UP rounding.
     * Returns zero when input is null.
     *
     * @param amount the amount to normalize
     * @return normalized amount (never null)
     */
    private BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
