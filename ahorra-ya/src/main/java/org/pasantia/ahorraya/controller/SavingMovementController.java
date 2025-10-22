package org.pasantia.ahorraya.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pasantia.ahorraya.dto.*;
import org.pasantia.ahorraya.dto.request.CreateCreditRequest;
import org.pasantia.ahorraya.dto.request.CreateDebitRequest;
import org.pasantia.ahorraya.model.ExpenseTransaction;
import org.pasantia.ahorraya.model.SavingAccount;
import org.pasantia.ahorraya.model.SavingMovement;
import org.pasantia.ahorraya.model.User;
import org.pasantia.ahorraya.service.ExpenseTransactionService;
import org.pasantia.ahorraya.service.SavingAccountService;
import org.pasantia.ahorraya.service.SavingMovementService;
import org.pasantia.ahorraya.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST controller that manages saving movements.
 *
 * <p>Provides endpoints to create credit and debit saving movements and to
 * retrieve saving movement data and statistics for users. Endpoints are secured
 * and delegate business rules to the corresponding services.</p>
 */
@RestController
@RequestMapping("/api/saving-movements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Saving Movements", description = "Saving movement management endpoints")
public class SavingMovementController {

    /**
     * Service that handles saving movement business operations.
     */
    private final SavingMovementService savingMovementService;

    /**
     * Service used to retrieve and operate on saving accounts.
     */
    private final SavingAccountService savingAccountService;

    /**
     * Service used to retrieve user information.
     */
    private final UserService userService;

    /**
     * Service used to retrieve expense transactions related to movements.
     */
    private final ExpenseTransactionService expenseTransactionService;

    /**
     * Create a credit saving movement for a user.
     *
     * @param userId id of the user performing the credit
     * @param request validated request containing account id, amount, optional expense transaction id and description
     * @return HTTP 201 with the created SavingMovement
     */
    @Operation(summary = "Create Credit Saving Movement", description = "Creates a credit saving movement for a user.")
    @PostMapping("/credit")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<SavingMovement> createCredit(
            @RequestParam UUID userId,
            @Valid @RequestBody CreateCreditRequest request) {

        log.info("Creating credit saving movement for user: {}", userId);

        User user = userService.getUserByIdOrThrow(userId);
        SavingAccount account = savingAccountService.getByIdOrThrow(request.getAccountId());

        ExpenseTransaction expenseTransaction = null;
        if (request.getExpenseTransactionId() != null) {
            expenseTransaction = expenseTransactionService.getByIdOrThrow(request.getExpenseTransactionId());
        }

        SavingMovement movement = savingMovementService.createCreditMovement(
                account,
                expenseTransaction,
                user,
                request.getAmount(),
                request.getDescription()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(movement);
    }

    /**
     * Create a debit saving movement for a user.
     *
     * @param userId id of the user performing the debit
     * @param request validated request containing account id, amount, optional expense transaction id and description
     * @return HTTP 201 with the created SavingMovement
     */
    @Operation(summary = "Create Debit Saving Movement", description = "Creates a debit saving movement for a user.")
    @PostMapping("/debit")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<SavingMovement> createDebit(
            @RequestParam UUID userId,
            @Valid @RequestBody CreateDebitRequest request) {

        log.info("Creating debit saving movement for user: {}", userId);

        User user = userService.getUserByIdOrThrow(userId);
        SavingAccount account = savingAccountService.getByIdOrThrow(request.getAccountId());

        ExpenseTransaction expenseTransaction = null;
        if (request.getExpenseTransactionId() != null) {
            expenseTransaction = expenseTransactionService.getByIdOrThrow(request.getExpenseTransactionId());
        }

        SavingMovement movement = savingMovementService.createDebitMovement(
                account,
                expenseTransaction,
                user,
                request.getAmount(),
                request.getDescription()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(movement);
    }

    /**
     * Retrieve all saving movements for a specific user.
     *
     * @param userId id of the user whose movements are requested
     * @return HTTP 200 with a list of SavingMovementDTO (may be empty)
     */
    @Operation(summary = "Get Saving Movements by User", description = "Retrieves all saving movements for a specific user.")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<SavingMovementDTO>> getByUser(@PathVariable UUID userId) {
        log.info("Retrieving saving movements for user: {}", userId);

        List<SavingMovement> movements = savingMovementService.getAllByUser(userId);
        List<SavingMovementDTO> dtoList = movements.stream()
                .map(savingMovementService::toDTO)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    /**
     * Retrieve savings statistics for a specific user.
     *
     * @param userId id of the user whose statistics are requested
     * @return HTTP 200 with SavingsStatsDTO
     */
    @Operation(summary = "Get User Savings Stats", description = "Retrieves savings statistics for a specific user.")
    @GetMapping("/user/{userId}/stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<SavingsStatsDTO> getStats(@PathVariable UUID userId) {
        log.info("Retrieving savings statistics for user: {}", userId);
        SavingsStatsDTO stats = savingMovementService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Retrieve the monthly savings evolution for a specific user.
     *
     * @param userId id of the user
     * @return HTTP 200 with a list of MonthlySavingsDTO representing monthly evolution
     */
    @Operation(summary = "Get Monthly Savings Evolution", description = "Retrieves the monthly savings evolution for a specific user.")
    @GetMapping("/user/{userId}/monthly-evolution")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<MonthlySavingsDTO>> getMonthlyEvolution(@PathVariable UUID userId) {
        log.info("Retrieving monthly savings evolution for user: {}", userId);
        List<MonthlySavingsDTO> evolution = savingMovementService.getMonthlyEvolution(userId);
        return ResponseEntity.ok(evolution);
    }

    /**
     * Retrieve the total savings for a user in a specific month and year.
     *
     * @param userId id of the user
     * @param month  month number (1-12)
     * @param year   four-digit year
     * @return HTTP 200 with the total saved amount for the given month and year
     */
    @Operation(summary = "Get Savings by Month", description = "Retrieves the total savings for a user in a specific month and year.")
    @GetMapping("/user/{userId}/savings")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<BigDecimal> getSavingsByMonth(
            @PathVariable UUID userId,
            @RequestParam int month,
            @RequestParam int year) {

        log.info("Retrieving savings for user {} for month: {}, year: {}", userId, month, year);
        BigDecimal total = savingMovementService.getSavingsByMonth(userId, month, year);
        return ResponseEntity.ok(total);
    }

    /**
     * Retrieve the total savings for a user within a specified date range.
     *
     * @param userId id of the user
     * @param start  inclusive start instant (ISO-8601)
     * @param end    inclusive end instant (ISO-8601)
     * @return HTTP 200 with the total saved amount in the range
     */
    @Operation(summary = "Get Savings in Date Range", description = "Retrieves the total savings for a user within a specified date range.")
    @GetMapping("/user/{userId}/savings-range")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<BigDecimal> getSavingsInRange(
            @PathVariable UUID userId,
            @RequestParam Instant start,
            @RequestParam Instant end) {

        log.info("Retrieving savings for user {} between {} and {}", userId, start, end);
        BigDecimal total = savingMovementService.getTotalSavingsInRange(userId, start, end);
        return ResponseEntity.ok(total);
    }
}