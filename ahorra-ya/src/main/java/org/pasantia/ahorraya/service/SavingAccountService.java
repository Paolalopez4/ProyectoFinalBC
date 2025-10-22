package org.pasantia.ahorraya.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pasantia.ahorraya.dto.AccountSummaryDTO;
import org.pasantia.ahorraya.model.SavingAccount;
import org.pasantia.ahorraya.model.User;
import org.pasantia.ahorraya.model.enums.SavingAccountStatus;
import org.pasantia.ahorraya.repository.SavingAccountRepository;
import org.pasantia.ahorraya.validation.BusinessException;
import org.pasantia.ahorraya.validation.savingaccountvalidations.AccountAlreadyDeletedException;
import org.pasantia.ahorraya.validation.savingaccountvalidations.AccountAlreadyExistsException;
import org.pasantia.ahorraya.validation.savingaccountvalidations.AccountNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for managing saving accounts.
 *
 * <p>Provides operations to create, retrieve, check existence and close saving accounts.
 * All persistence operations are delegated to {@link SavingAccountRepository}. Methods
 * enforce business rules (e.g. single active account per user, cannot close account with balance).</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SavingAccountService {

    /**
     * Repository used to persist and query SavingAccount entities.
     */
    private final SavingAccountRepository savingAccountRepository;

    /**
     * Create a new saving account for the given user.
     *
     * @param user the owner of the new saving account (must not be null)
     * @return the persisted SavingAccount
     * @throws IllegalArgumentException         if the provided user is null
     * @throws AccountAlreadyExistsException    if the user already has an active (non-deleted) account
     */
    @Transactional
    public SavingAccount createNewAccount(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        boolean hasActive = user.getAccounts().stream()
                .anyMatch(a -> a.getStatus() == SavingAccountStatus.ACTIVE && !a.isDeleted());

        if (hasActive) {
            log.warn("Attempt to create duplicate account for user: {}", user.getId());
            throw new AccountAlreadyExistsException(user.getId());
        }

        SavingAccount account = new SavingAccount(
                user,
                BigDecimal.ZERO,
                SavingAccountStatus.ACTIVE,
                BigDecimal.ZERO
        );

        SavingAccount saved = savingAccountRepository.save(account);
        log.info("Saving account created: {} for user: {}",
                saved.getAccountNumber(), user.getId());
        return saved;
    }

    /**
     * Retrieve all active (not deleted) saving accounts.
     *
     * @return list of active SavingAccount entities (may be empty)
     */
    public List<SavingAccount> getAllActiveAccounts() {
        return savingAccountRepository.findAll().stream()
                .filter(account -> !account.isDeleted())
                .toList();
    }

    /**
     * Retrieve a saving account by id or throw if not found or deleted.
     *
     * @param id the account UUID
     * @return the SavingAccount
     * @throws AccountNotFoundException when the account does not exist or is deleted
     */
    public SavingAccount getByIdOrThrow(UUID id) {
        return savingAccountRepository.findById(id)
                .filter(account -> !account.isDeleted())
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    /**
     * Retrieve a saving account by id.
     *
     * @param id the account UUID
     * @return Optional with the SavingAccount if present and not deleted
     */
    public Optional<SavingAccount> getById(UUID id) {
        return savingAccountRepository.findById(id)
                .filter(account -> !account.isDeleted());
    }

    /**
     * Retrieve an active (not deleted) saving account for a given user or throw if none found.
     *
     * @param userId the user's UUID
     * @return the SavingAccount associated with the user
     * @throws AccountNotFoundException when no active account exists for the user
     */
    public SavingAccount getByUserIdOrThrow(UUID userId) {
        return savingAccountRepository.findByUserId(userId)
                .filter(account -> !account.isDeleted())
                .orElseThrow(() -> new AccountNotFoundException(
                        "No active account found for user: " + userId));
    }

    /**
     * Retrieve an active (not deleted) saving account for a given user.
     *
     * @param userId the user's UUID
     * @return Optional with the SavingAccount if present and not deleted
     */
    public Optional<SavingAccount> getByUserId(UUID userId) {
        return savingAccountRepository.findByUserId(userId)
                .filter(account -> !account.isDeleted());
    }

    /**
     * Retrieve a saving account by account number (if not deleted).
     *
     * @param accountNumber the account number string
     * @return Optional with the SavingAccount if present and not deleted
     */
    public Optional<SavingAccount> getByAccountNumber(String accountNumber) {
        return savingAccountRepository.findByAccountNumber(accountNumber)
                .filter(account -> !account.isDeleted());
    }

    /**
     * Retrieve a summary of the saving account for a specific user.
     *
     * @param userId the user's UUID
     * @return AccountSummaryDTO containing account summary information
     */
    public AccountSummaryDTO getAccountSummary(UUID userId) {
        return savingAccountRepository.getAccountSummary(userId);
    }

    /**
     * Check whether a user currently has an active saving account.
     *
     * @param userId the user's UUID
     * @return true if an active, non-deleted account exists for the user
     */
    public boolean userHasActiveAccount(UUID userId) {
        return savingAccountRepository.existsByUserIdAndStatusAndIsDeletedFalse(
                userId, SavingAccountStatus.ACTIVE);
    }

    /**
     * Close (soft delete) a saving account by marking it as deleted.
     *
     * <p>Business rules:
     * - Cannot close an already deleted account.
     * - Cannot close an account with a non-zero balance.</p>
     *
     * @param id the account UUID to close
     * @throws AccountNotFoundException       when the account does not exist
     * @throws AccountAlreadyDeletedException when the account is already deleted
     * @throws BusinessException              when the account has a non-zero balance
     */
    @Transactional
    public void closeAccount(UUID id) {
        SavingAccount account = savingAccountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (account.isDeleted()) {
            throw new AccountAlreadyDeletedException(id);
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(
                    "Cannot close an account with a non-zero balance. Current balance: $" +
                            account.getBalance());
        }

        account.markAsDeleted();
        savingAccountRepository.save(account);
        log.info("Account closed: {}", account.getAccountNumber());
    }
}
