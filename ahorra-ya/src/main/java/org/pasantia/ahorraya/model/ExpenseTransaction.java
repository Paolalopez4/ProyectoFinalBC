package org.pasantia.ahorraya.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.pasantia.ahorraya.model.enums.ExpenseCategory;
import org.pasantia.ahorraya.model.enums.ExpenseStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing an expense transaction made by a user.
 * <p>
 * Stores the original and rounded amounts, savings difference, description,
 * category, merchant, transaction date, status, and audit information.
 * Supports micro-saving logic by rounding up expenses and tracking the difference.
 * </p>
 */
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "expense_transactions")
@Entity
public class ExpenseTransaction {
    /**
     * Unique identifier for this expense transaction (UUID).
     */
    @Getter
    @Id
    @UuidGenerator
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * The user who made this expense transaction.
     */
    @Getter
    @Setter
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The original amount of the expense before rounding.
     */
    @Getter
    @Setter
    @NotNull
    @Column(name = "original_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal originalAmount;

    /**
     * The rounded amount of the expense after applying micro-saving logic.
     */
    @Getter
    @Setter
    @NotNull
    @Column(name = "rounded_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal roundedAmount;

    /**
     * The difference between the rounded amount and the original amount (savings).
     */
    @Getter
    @NotNull
    @Column(name = "savings_difference", nullable = false, precision = 19, scale = 2)
    private BigDecimal savingsDifference;

    /**
     * Description of the expense.
     */
    @Getter
    @Setter
    @NotNull
    @NotBlank
    @Column(nullable = false)
    private String description;

    /**
     * Category of the expense.
     */
    @Getter
    @Setter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "expense_category_id",nullable = false)
    private ExpenseCategory category;

    /**
     * Merchant where the expense was made.
     */
    @Getter
    @Setter
    @NotNull
    @NotBlank
    @Column(nullable = false)
    private String merchant;

    /**
     * Date and time when the transaction occurred.
     */
    @Getter
    @NotNull
    @Column(name = "transaction_date", nullable = false, updatable = false)
    private Instant transactionDate;

    /**
     * Status of the expense transaction.
     */
    @Getter
    @Setter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "expense_status_id",nullable = false)
    private ExpenseStatus status;

    /**
     * Embedded audit information (created/updated timestamps).
     */
    @Getter
    @Embedded
    private final Audit audit = new Audit();

    /**
     * Constructs a new ExpenseTransaction with the specified parameters.
     *
     * @param user the user who made the transaction
     * @param originalAmount the original amount of the expense
     * @param roundedAmount the rounded amount after applying micro-saving logic
     * @param description description of the expense
     * @param category category of the expense
     * @param merchant merchant where the expense was made
     * @param transactionDate date and time of the transaction (if null, set to now)
     * @param status status of the expense transaction
     */
    public ExpenseTransaction(User user, BigDecimal originalAmount, BigDecimal roundedAmount, String description, ExpenseCategory category, String merchant, Instant transactionDate, ExpenseStatus status) {
        this.user = user;
        this.originalAmount = normalize(originalAmount);
        this.roundedAmount = normalize(roundedAmount);
        this.description = description;
        this.category = category;
        this.merchant = merchant;
        this.transactionDate = transactionDate == null ? Instant.now() : transactionDate;
        this.status = status;
        calculateSavingsDifference();
    }

    /**
     * JPA lifecycle callback executed before the entity is persisted.
     * Normalizes amounts, calculates savings difference, sets transaction date if null,
     * and updates audit information.
     */
    @PrePersist
    private void prePersist() {
        this.originalAmount = normalize(this.originalAmount);
        this.roundedAmount = normalize(this.roundedAmount);
        if (this.savingsDifference == null) {
            calculateSavingsDifference();
        }
        if (this.transactionDate == null) {
            this.transactionDate = Instant.now();
        }
        this.audit.prePersist();
    }

    /**
     * JPA lifecycle callback executed before the entity is updated.
     * Normalizes amounts, recalculates savings difference, and updates audit timestamp.
     */
    @PreUpdate
    private void preUpdate() {
        this.originalAmount = normalize(this.originalAmount);
        this.roundedAmount = normalize(this.roundedAmount);
        calculateSavingsDifference();
        this.audit.updateTimestamp();
    }

    /**
     * Calculates the savings difference as the rounded amount minus the original amount.
     */
    public void calculateSavingsDifference() {
        BigDecimal o = normalize(this.originalAmount);
        BigDecimal r = normalize(this.roundedAmount);
        this.savingsDifference = r.subtract(o).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Applies rounding logic based on the provided micro-saving configuration.
     * <p>
     * If the configuration is active, rounds up the original amount to the nearest whole number.
     * If the configuration is inactive or null, keeps the original amount unchanged.
     * </p>
     *
     * @param config the micro-saving configuration to use (can be null)
     */
    public void applyRounding(MicroSavingConfig config) {
        if (this.originalAmount == null) {
            this.roundedAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            calculateSavingsDifference();
            return;
        }

        if (config == null || !config.isActive()) {
            this.roundedAmount = this.originalAmount.setScale(2, RoundingMode.HALF_UP);
            calculateSavingsDifference();
            return;
        }

        this.roundedAmount = this.originalAmount
                .setScale(0, RoundingMode.UP)
                .setScale(2, RoundingMode.HALF_UP);
        calculateSavingsDifference();
    }

    /**
     * Marks this expense transaction as processed.
     */
    public void markAsProcessed() {
        this.status = ExpenseStatus.PROCESSED;
    }

    /**
     * Marks this expense transaction as rejected.
     */
    public void markAsRejected() {
        this.status = ExpenseStatus.REJECTED;
    }

    /**
     * Normalizes a BigDecimal value to scale 2, or returns zero if null.
     *
     * @param value the value to normalize
     * @return the normalized value
     */
    private static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseTransaction)) return false;
        ExpenseTransaction that = (ExpenseTransaction) o;
        return id != null && id.equals(that.id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
