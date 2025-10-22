package org.pasantia.ahorraya.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pasantia.ahorraya.dto.MonthlySavingsDTO;
import org.pasantia.ahorraya.dto.SavingMovementDTO;
import org.pasantia.ahorraya.dto.SavingsStatsDTO;
import org.pasantia.ahorraya.model.ExpenseTransaction;
import org.pasantia.ahorraya.model.SavingAccount;
import org.pasantia.ahorraya.model.SavingMovement;
import org.pasantia.ahorraya.model.User;
import org.pasantia.ahorraya.model.enums.MovementType;
import org.pasantia.ahorraya.model.enums.SavingAccountStatus;
import org.pasantia.ahorraya.repository.SavingMovementRepository;
import org.pasantia.ahorraya.validation.savingaccountvalidations.AccountAlreadyDeletedException;
import org.pasantia.ahorraya.validation.savingaccountvalidations.InactiveAccountException;
import org.pasantia.ahorraya.validation.savingmovementvalidations.InsufficientBalanceException;
import org.pasantia.ahorraya.validation.savingmovementvalidations.InvalidAmountException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for creating and querying saving movements (credits and debits).
 *
 * <p>Performs validation of inputs, enforces account state rules (active/not-deleted),
 * applies movements to accounts and persists resulting SavingMovement entities via the
 * {@link SavingMovementRepository}.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SavingMovementService {

    /**
     * Repository used to persist and query SavingMovement entities.
     */
    private final SavingMovementRepository savingMovementRepository;

    /**
     * Create and persist a credit movement for a saving account.
     *
     * @param account          target saving account
     * @param expenseTransaction optional related expense transaction
     * @param user             user performing the movement
     * @param amount           amount to credit (must be > 0)
     * @param description      short description of the movement
     * @return persisted SavingMovement
     * @throws IllegalArgumentException when input arguments are invalid
     * @throws InactiveAccountException when the account is not active
     * @throws AccountAlreadyDeletedException when the account is marked as deleted
     */
    @Transactional
    public SavingMovement createCreditMovement(
            SavingAccount account,
            ExpenseTransaction expenseTransaction,
            User user,
            BigDecimal amount,
            String description
    ) {
        validateMovementInputs(account, user, amount, description);

        if (account.getStatus() != SavingAccountStatus.ACTIVE) {
            throw new InactiveAccountException(account.getAccountNumber());
        }

        if (account.isDeleted()) {
            throw new AccountAlreadyDeletedException(account.getId());
        }

        SavingMovement movement = new SavingMovement(
                account,
                expenseTransaction,
                user,
                amount,
                MovementType.CREDIT,
                description
        );

        movement.applyMovement();
        SavingMovement saved = savingMovementRepository.save(movement);

        log.info("Credit movement created by {}: +${} | Account: {}",
                user.getUsername(), amount, account.getAccountNumber());

        return saved;
    }

    /**
     * Create and persist a debit movement for a saving account.
     *
     * @param account          target saving account
     * @param expenseTransaction optional related expense transaction
     * @param user             user performing the movement
     * @param amount           amount to debit (must be > 0 and <= account balance)
     * @param description      short description of the movement
     * @return persisted SavingMovement
     * @throws IllegalArgumentException when input arguments are invalid
     * @throws InactiveAccountException when the account is not active
     * @throws AccountAlreadyDeletedException when the account is marked as deleted
     * @throws InsufficientBalanceException when the account balance is insufficient
     */
    @Transactional
    public SavingMovement createDebitMovement(
            SavingAccount account,
            ExpenseTransaction expenseTransaction,
            User user,
            BigDecimal amount,
            String description
    ) {
        validateMovementInputs(account, user, amount, description);

        if (account.getStatus() != SavingAccountStatus.ACTIVE) {
            throw new InactiveAccountException(account.getAccountNumber());
        }

        if (account.isDeleted()) {
            throw new AccountAlreadyDeletedException(account.getId());
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(account.getBalance(), amount);
        }

        SavingMovement movement = new SavingMovement(
                account,
                expenseTransaction,
                user,
                amount,
                MovementType.DEBIT,
                description
        );

        movement.applyMovement();
        SavingMovement saved = savingMovementRepository.save(movement);

        log.info("Debit movement created by {}: -${} | Account: {}",
                user.getUsername(), amount, account.getAccountNumber());

        return saved;
    }

    /**
     * Validate common inputs for creating a movement.
     *
     * @param account     target saving account (must not be null)
     * @param user        user performing the movement (must not be null)
     * @param amount      movement amount (must be > 0)
     * @param description movement description (must not be blank)
     * @throws IllegalArgumentException when account or user or description is invalid
     * @throws InvalidAmountException   when the amount is null or not greater than zero
     */
    private void validateMovementInputs(SavingAccount account, User user,
                                        BigDecimal amount, String description) {
        if (account == null) {
            throw new IllegalArgumentException("Saving account must not be null");
        }

        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Amount must be greater than zero. Received: " + amount);
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be empty");
        }
    }

    /**
     * Retrieve all saving movements for a given user.
     *
     * @param userId the user's UUID
     * @return list of SavingMovement for the user
     */
    public List<SavingMovement> getAllByUser(UUID userId) {
        return savingMovementRepository.findAll().stream()
                .filter(sm -> sm.getUser().getId().equals(userId))
                .toList();
    }

    /**
     * Count saving movements for a specific user.
     *
     * @param userId the user's UUID
     * @return number of saving movements
     */
    public Long countByUser(UUID userId) {
        return savingMovementRepository.countByUserId(userId);
    }

    /**
     * Retrieve aggregated savings statistics for a user.
     *
     * @param userId the user's UUID
     * @return SavingsStatsDTO containing aggregated stats
     */
    public SavingsStatsDTO getUserStats(UUID userId) {
        return savingMovementRepository.getUserSavingsStats(userId);
    }

    /**
     * Retrieve monthly savings evolution for a user.
     *
     * @param userId the user's UUID
     * @return list of MonthlySavingsDTO
     */
    public List<MonthlySavingsDTO> getMonthlyEvolution(UUID userId) {
        return savingMovementRepository.getMonthlySavingsEvolution(userId).stream()
                .map(row -> new MonthlySavingsDTO(
                        ((Integer) row[0]),
                        ((Integer) row[1]),
                        row[2] instanceof BigDecimal bd ? bd :
                                BigDecimal.valueOf(((Number) row[2]).doubleValue()),
                        ((Long) row[3]),
                        row[4] instanceof BigDecimal bd2 ? bd2 :
                                BigDecimal.valueOf(((Number) row[4]).doubleValue())
                ))
                .toList();
    }

    /**
     * Retrieve total savings for a user in a specific month and year.
     *
     * @param userId the user's UUID
     * @param month   month number (1-12)
     * @param year    four-digit year
     * @return total saved amount for the month
     * @throws IllegalArgumentException when month or year are out of allowed range
     */
    public BigDecimal getSavingsByMonth(UUID userId, int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Invalid month. Must be between 1 and 12. Received: " + month);
        }

        if (year < 2000 || year > Year.now().getValue()) {
            throw new IllegalArgumentException(
                    "Invalid year. Must be between 2000 and " + Year.now().getValue());
        }

        return savingMovementRepository.getSavingsByMonth(userId, month, year);
    }

    /**
     * Retrieve total savings for a user between two instants.
     *
     * @param userId the user's UUID
     * @param start  inclusive start instant
     * @param end    inclusive end instant
     * @return total saved amount in the range
     * @throws IllegalArgumentException when dates are null or start is after end
     */
    public BigDecimal getTotalSavingsInRange(UUID userId, Instant start, Instant end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException(
                    "Start date must be before end date");
        }

        return savingMovementRepository.getTotalSavingsInRange(userId, start, end);
    }

    /**
     * Convert a SavingMovement entity to its DTO representation.
     *
     * @param movement the SavingMovement entity
     * @return SavingMovementDTO populated from the entity
     */
    public SavingMovementDTO toDTO(SavingMovement movement) {
        return new SavingMovementDTO(
                movement.getId(),
                movement.getSavingsAmount(),
                movement.getMovementType(),
                movement.getDescription(),
                movement.getAudit().getCreatedAt(),
                movement.getSavingAccount().getId(),
                movement.getSavingAccount().getBalance(),
                movement.getExpenseTransaction() != null ?
                        movement.getExpenseTransaction().getId() : null
        );
    }
}