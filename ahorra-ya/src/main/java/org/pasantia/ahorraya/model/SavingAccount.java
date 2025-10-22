package org.pasantia.ahorraya.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.pasantia.ahorraya.model.enums.SavingAccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a saving account within the system.
 * Holds balance, unique account number, status and embedded audit data.
 * Business operations update balance, historical totals and last movement timestamp.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "saving_accounts")
@Entity
public class SavingAccount {
    /**
     * Unique identifier of the account.
     * Generated as a UUID; not updatable and cannot be null.
     */
    @Getter
    @NotNull
    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Setter
    @Getter
    @JsonIgnore
    @OneToMany(mappedBy = "savingAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavingMovement> savingMovements = new ArrayList<>();

    /**
     * Account owner (user).
     * Many-to-one relation to User; cannot be null.
     */
    @Getter
    @Setter
    @NotNull
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Unique account number string.
     * Generated at creation time; cannot be null and must be unique.
     */
    @Getter
    @NotNull
    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    /**
     * Current account balance.
     * Normalized to 2 decimal places on persist/update.
     */
    @Getter
    @NotNull
    @Column(nullable = false)
    private BigDecimal balance;

    /**
     * Account status (e.g., ACTIVE, INACTIVE).
     * Persisted using EnumType.STRING.
     */
    @Getter
    @Setter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "savings_account_status_id", nullable = false)
    private SavingAccountStatus status;

    /**
     * Historical total of savings accumulated in this account.
     * Increased when the account is credited; normalized to 2 decimals.
     */
    @Getter
    @NotNull
    @Column(name = "total_historical_savings", nullable = false)
    private BigDecimal totalHistoricalSavings;

    /**
     * Timestamp of the last movement (credit or debit).
     * Updated when business operations occur.
     */
    @Getter
    @NotNull
    @Column(name = "last_movement_at", nullable = false)
    private Instant lastMovementAt;

    @Getter
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    /**
     * Embedded audit information for creation and update tracking.
     */
    @Getter
    @NotNull
    @Embedded
    private final Audit audit = new Audit();

    /**
     * Constructs a new SavingAccount for a user.
     *
     * @param user                   owner of the account; must not be null.
     * @param balance                initial balance; if null, initialized to BigDecimal.ZERO.
     * @param status                 initial account status; if null, defaults to SavingAccountStatus.ACTIVE.
     * @param totalHistoricalSavings initial historical savings; if null, initialized to BigDecimal.ZERO.
     */
    public SavingAccount(User user, BigDecimal balance, SavingAccountStatus status, BigDecimal totalHistoricalSavings) {
        this.user = user;
        this.accountNumber = generateAccountNumber();
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.status = status != null ? status : SavingAccountStatus.ACTIVE;
        this.totalHistoricalSavings = totalHistoricalSavings != null ? totalHistoricalSavings : BigDecimal.ZERO;
        this.lastMovementAt = Instant.now();
    }

    /**
     * Credits (adds) an amount to the account balance.
     *
     * @param amount amount to credit; must be greater than zero.
     * @throws IllegalArgumentException if amount is null or <= 0.
     * @throws IllegalStateException    if the account is not ACTIVE.
     */
    void credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The amount must be greater than zero");
        }
        if (this.status != SavingAccountStatus.ACTIVE) {
            throw new IllegalStateException("The account is not active");
        }
        this.balance = this.balance.add(amount);
        this.totalHistoricalSavings = this.totalHistoricalSavings.add(amount);
        this.lastMovementAt = Instant.now();
    }

    /**
     * Debits (subtracts) an amount from the account balance.
     *
     * @param amount amount to debit; must be greater than zero and less than or equal to available balance.
     * @throws IllegalArgumentException if amount is null or <= 0.
     * @throws IllegalStateException    if the account is not ACTIVE or if there are insufficient funds.
     */
     void debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The amount must be greater than zero");
        }
        if (this.status != SavingAccountStatus.ACTIVE) {
            throw new IllegalStateException("The account is not active");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insuficient funds in the account");
        }

        this.balance = this.balance.subtract(amount);
        this.lastMovementAt = Instant.now();
    }

    public void markAsDeleted() {
        this.isDeleted = true;
        this.status = SavingAccountStatus.INACTIVE;
    }

    /**
     * Generates a unique account number composed of a timestamp and a random component.
     * Approximate format: "SA" + 9 timestamp digits + 4 random digits.
     *
     * @return generated account number.
     */
    private static String generateAccountNumber() {
        long timestamp = System.currentTimeMillis() % 1000000000;
        int random = (int) (Math.random() * 9000) + 1000;
        return String.format("SA%09d%04d", timestamp, random);
    }

    /**
     * JPA callback executed before the entity is persisted.
     * Normalizes monetary fields to 2 decimals, ensures lastMovementAt and delegates audit pre-persist handling.
     */
    @PrePersist
    private void prePersist() {
        this.balance = normalize(this.balance);
        this.totalHistoricalSavings = normalize(this.totalHistoricalSavings);
        if (this.lastMovementAt == null) {
            this.lastMovementAt = Instant.now();
        }
        this.audit.prePersist();
    }

    /**
     * JPA callback executed before the entity is updated.
     * Normalizes monetary fields to 2 decimals, updates lastMovementAt and audit timestamp.
     */
    @PreUpdate
    private void preUpdate() {
        this.balance = normalize(this.balance);
        this.totalHistoricalSavings = normalize(this.totalHistoricalSavings);
        this.lastMovementAt = Instant.now();
        this.audit.updateTimestamp();
    }

    /**
     * Normalizes a BigDecimal to scale 2 using HALF_UP rounding.
     * Returns BigDecimal.ZERO when the input is null.
     *
     * @param amount value to normalize.
     * @return normalized value with 2 decimal places.
     */
    private BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
