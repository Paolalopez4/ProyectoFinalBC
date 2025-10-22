package org.pasantia.ahorraya.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pasantia.ahorraya.dto.AccountSummaryDTO;
import org.pasantia.ahorraya.model.SavingAccount;
import org.pasantia.ahorraya.model.User;
import org.pasantia.ahorraya.service.SavingAccountService;
import org.pasantia.ahorraya.validation.savingaccountvalidations.AccountNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller that exposes endpoints to manage saving accounts.
 *
 * <p>Provides operations to create, retrieve, check and close saving accounts.
 * Methods are secured with role-based annotations and delegate business logic to
 * SavingAccountService.</p>
 */
@RestController
@RequestMapping("/api/saving-accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Saving Accounts", description = "Saving accounts management endpoints")
public class SavingAccountController {

    /**
     * Service that encapsulates business logic for saving account operations.
     */
    private final SavingAccountService savingAccountService;

    /**
     * Create a new saving account for the authenticated user.
     *
     * @param auth authentication token containing the current user principal
     * @return HTTP 201 with the created SavingAccount
     */
    @Operation(summary = "Create New Saving Account", description = "Creates a new saving account for the authenticated user.")
    @PostMapping("/new")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<SavingAccount> createNewAccount(Authentication auth) {
        User currentUser = (User) auth.getPrincipal();
        log.info("Creating new saving account for user: {}", currentUser.getUsername());

        SavingAccount newAccount = savingAccountService.createNewAccount(currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAccount);
    }

    /**
     * Retrieve all active saving accounts (admin only).
     *
     * @return HTTP 200 with the list of active SavingAccount objects
     */
    @Operation(summary = "Get All Active Saving Accounts", description = "Retrieves all active saving accounts. Admin access required.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SavingAccount>> getAllActiveAccounts() {
        log.info("Retrieving all active saving accounts");
        return ResponseEntity.ok(savingAccountService.getAllActiveAccounts());
    }

    /**
     * Retrieve a saving account by its unique identifier.
     *
     * @param id UUID of the saving account
     * @return HTTP 200 with the SavingAccount if found
     */
    @Operation(summary = "Get Saving Account by ID", description = "Retrieves a saving account by its unique identifier.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<SavingAccount> getById(@PathVariable UUID id) {
        log.info("Retrieving saving account: {}", id);
        SavingAccount account = savingAccountService.getByIdOrThrow(id);
        return ResponseEntity.ok(account);
    }

    /**
     * Retrieve the saving account associated with a specific user.
     *
     * @param userId UUID of the user
     * @return HTTP 200 with the SavingAccount if found
     */
    @Operation(summary = "Get Saving Account by User ID", description = "Retrieves the saving account associated with a specific user.")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<SavingAccount> getByUserId(@PathVariable UUID userId) {
        log.info("Retrieving saving account for user: {}", userId);
        SavingAccount account = savingAccountService.getByUserIdOrThrow(userId);
        return ResponseEntity.ok(account);
    }

    /**
     * Retrieve a saving account by its account number.
     *
     * @param accountNumber the account number string
     * @return HTTP 200 with the SavingAccount if found
     * @throws AccountNotFoundException if no account matches the provided number
     */
    @Operation(summary = "Get Saving Account by Account Number", description = "Retrieves a saving account by its account number.")
    @GetMapping("/number/{accountNumber}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<SavingAccount> getByAccountNumber(@PathVariable String accountNumber) {
        log.info("Retrieving saving account by account number: {}", accountNumber);
        return savingAccountService.getByAccountNumber(accountNumber)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
    }

    /**
     * Retrieve a summary of the saving account for a specific user.
     *
     * @param userId UUID of the user
     * @return HTTP 200 with AccountSummaryDTO
     */
    @Operation(summary = "Get Account Summary", description = "Retrieves a summary of the saving account for a specific user.")
    @GetMapping("/user/{userId}/summary")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AccountSummaryDTO> getAccountSummary(@PathVariable UUID userId) {
        log.info("Retrieving account summary for user: {}", userId);
        return ResponseEntity.ok(savingAccountService.getAccountSummary(userId));
    }

    /**
     * Check whether a user has an active saving account.
     *
     * @param userId UUID of the user to check
     * @return HTTP 200 with true if user has an active account, false otherwise
     */
    @Operation(summary = "Check if User has Active Saving Account", description = "Checks if a specific user has an active saving account.")
    @GetMapping("/user/{userId}/has-active")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Boolean> hasActiveAccount(@PathVariable UUID userId) {
        log.info("Checking whether user {} has an active saving account", userId);
        return ResponseEntity.ok(savingAccountService.userHasActiveAccount(userId));
    }

    /**
     * Close (delete) a saving account by its identifier (admin only).
     *
     * @param accountId UUID of the account to close
     * @return HTTP 204 No Content on success
     */
    @Operation(summary = "Close Saving Account", description = "Closes a saving account by its unique identifier. Admin access required.")
    @DeleteMapping("/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID accountId) {
        log.info("Closing saving account: {}", accountId);
        savingAccountService.closeAccount(accountId);
        return ResponseEntity.noContent().build();
    }
}