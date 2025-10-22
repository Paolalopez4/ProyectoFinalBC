package org.pasantia.ahorraya.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pasantia.ahorraya.dto.request.CreateExpenseRequest;
import org.pasantia.ahorraya.dto.MerchantSavingsDTO;
import org.pasantia.ahorraya.model.ExpenseTransaction;
import org.pasantia.ahorraya.model.MicroSavingConfig;
import org.pasantia.ahorraya.model.SavingAccount;
import org.pasantia.ahorraya.model.User;
import org.pasantia.ahorraya.model.enums.ExpenseStatus;
import org.pasantia.ahorraya.repository.ExpenseTransactionRepository;
import org.pasantia.ahorraya.repository.MicroSavingConfigRepository;
import org.pasantia.ahorraya.repository.UserRepository;
import org.pasantia.ahorraya.validation.expensetransactionvalidations.ExpenseTransactionNotFoundException;
import org.pasantia.ahorraya.validation.savingmovementvalidations.InvalidAmountException;
import org.pasantia.ahorraya.validation.uservalidations.UserNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for creating and managing expense transactions.
 *
 * <p>This service handles validation, persistence and the business logic that
 * applies micro-saving rules to expenses. When savings are produced, it ensures
 * the user has an active saving account and creates the appropriate saving movements.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExpenseTransactionService {

    /**
     * Repository for ExpenseTransaction persistence and queries.
     */
    private final ExpenseTransactionRepository repository;

    /**
     * Repository used to lookup users by id.
     */
    private final UserRepository userRepository;

    /**
     * Repository used to obtain micro-saving configurations for users.
     */
    private final MicroSavingConfigRepository configRepository;

    /**
     * Service that manages saving accounts.
     */
    private final SavingAccountService savingAccountService;

    /**
     * Service that manages saving movements (credits/debits).
     */
    private final SavingMovementService savingMovementService;

    /**
     * Retrieve all expense transactions belonging to a given user.
     *
     * @param userId the user's UUID
     * @return list of ExpenseTransaction for the user (may be empty)
     */
    public List<ExpenseTransaction> getAllByUser(UUID userId) {
        return repository.findAll().stream()
                .filter(et -> et.getUser().getId().equals(userId))
                .toList();
    }

    /**
     * Retrieve an expense transaction by id or throw if not found.
     *
     * @param id the transaction UUID
     * @return the ExpenseTransaction
     * @throws ExpenseTransactionNotFoundException if no transaction with the id exists
     */
    public ExpenseTransaction getByIdOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ExpenseTransactionNotFoundException(id));
    }

    /**
     * Retrieve an expense transaction by id.
     *
     * @param id the transaction UUID
     * @return Optional containing the ExpenseTransaction if found
     */
    public Optional<ExpenseTransaction> getById(UUID id) {
        return repository.findById(id);
    }

    /**
     * Create a new expense transaction for a user, apply micro-saving rules,
     * persist the transaction and create saving movements if applicable.
     *
     * @param userId  UUID of the user creating the expense
     * @param request validated CreateExpenseRequest containing expense details
     * @return the persisted ExpenseTransaction
     * @throws IllegalArgumentException if the request or required values are invalid
     * @throws UserNotFoundException    if the user does not exist
     */
    @Transactional
    public ExpenseTransaction createExpenseTransaction(UUID userId, CreateExpenseRequest request) {
        log.info("Creating expense transaction for user: {}", userId);

        validateExpenseRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Optional<MicroSavingConfig> configOpt = configRepository.findByUserIdAndActiveTrue(userId);

        ExpenseTransaction transaction = new ExpenseTransaction(
                user,
                request.getOriginalAmount(),
                request.getOriginalAmount(),
                request.getDescription(),
                request.getCategory(),
                request.getMerchant(),
                Instant.now(),
                ExpenseStatus.PENDING
        );

        configOpt.ifPresent(transaction::applyRounding);

        transaction.markAsProcessed();

        ExpenseTransaction savedTransaction = repository.save(transaction);
        log.info("Transaction created. Savings difference: ${}", savedTransaction.getSavingsDifference());

        BigDecimal difference = savedTransaction.getSavingsDifference();
        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            processSavings(user, savedTransaction, difference);
        }

        return savedTransaction;
    }

    /**
     * Ensure the user has an active saving account and credit the savings difference.
     *
     * @param user        the User who generated the expense
     * @param transaction the ExpenseTransaction that produced savings
     * @param difference  the amount to credit as savings
     */
    private void processSavings(User user, ExpenseTransaction transaction, BigDecimal difference) {
        if (!savingAccountService.userHasActiveAccount(user.getId())) {
            savingAccountService.createNewAccount(user);
            log.info("Savings account automatically created for user: {}", user.getId());
        }

        SavingAccount account = savingAccountService.getByUserIdOrThrow(user.getId());

        savingMovementService.createCreditMovement(
                account,
                transaction,
                user,
                difference,
                "Purchase savings at " + transaction.getMerchant()
        );

        log.info("Saved ${} credited to account of {}", difference, user.getUsername());
    }

    /**
     * Validate the incoming CreateExpenseRequest for required fields and positive amounts.
     *
     * @param request the incoming request to validate
     * @throws IllegalArgumentException if required fields are missing or invalid
     * @throws InvalidAmountException   if the original amount is not greater than zero
     */
    private void validateExpenseRequest(CreateExpenseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }

        if (request.getOriginalAmount() == null ||
                request.getOriginalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Original amount must be greater than zero. Received: " +
                            request.getOriginalAmount());
        }

        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description must not be empty");
        }

        if (request.getMerchant() == null || request.getMerchant().isBlank()) {
            throw new IllegalArgumentException("Merchant must not be empty");
        }

        if (request.getCategory() == null) {
            throw new IllegalArgumentException("Category must not be null");
        }
    }

    /**
     * Delete an expense transaction by id.
     *
     * @param id UUID of the transaction to delete
     * @throws ExpenseTransactionNotFoundException if the transaction does not exist
     */
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ExpenseTransactionNotFoundException(id);
        }
        repository.deleteById(id);
        log.info("Expense transaction deleted: {}", id);
    }

    /**
     * Retrieve processed transactions for a user within a time range.
     *
     * @param userId the user's UUID
     * @param start  inclusive start Instant
     * @param end    inclusive end Instant
     * @return list of processed ExpenseTransaction within the period
     * @throws IllegalArgumentException if dates are null or start is after end
     */
    public List<ExpenseTransaction> getProcessedTransactionsByPeriod(
            UUID userId, Instant start, Instant end) {

        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates must not be null");
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException(
                    "Start date must be before end date");
        }

        return repository.getTransactionsByPeriod(userId, start, end);
    }

    /**
     * Count processed transactions for a user filtered by status for a given month and year.
     *
     * @param userId the user's UUID
     * @param status the ExpenseStatus to filter
     * @param month  month number (1-12)
     * @param year   four-digit year
     * @return count of matching transactions
     * @throws IllegalArgumentException if month is invalid
     */
    public Long countProcessedTransactionsByMonth(
            UUID userId, ExpenseStatus status, int month, int year) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }

        return repository.countTransactionsByMonth(userId, status, month, year);
    }

    /**
     * Retrieve top merchants where the user saved the most, limited by the provided limit.
     *
     * @param userId the user's UUID
     * @param limit  maximum number of merchants to return
     * @return list of MerchantSavingsDTO ordered by savings descending
     * @throws IllegalArgumentException if limit is not positive
     */
    public List<MerchantSavingsDTO> getTopMerchants(UUID userId, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }

        return repository.getTopMerchants(userId, PageRequest.of(0, limit));
    }
}

