package org.pasantia.ahorraya.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pasantia.ahorraya.dto.request.CreateExpenseRequest;
import org.pasantia.ahorraya.dto.MerchantSavingsDTO;
import org.pasantia.ahorraya.model.ExpenseTransaction;
import org.pasantia.ahorraya.model.enums.ExpenseStatus;
import org.pasantia.ahorraya.service.ExpenseTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller that exposes endpoints to manage expense transactions.
 *
 * <p>Provides endpoints to create, retrieve, count and delete expense transactions
 * for users. All endpoints return appropriate HTTP responses and delegate business
 * logic to ExpenseTransactionService.</p>
 */
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expense Transactions", description = "Expense transactions management endpoints")
public class ExpenseTransactionController {

    /**
     * Service that performs business operations related to expense transactions.
     * Injected via constructor (Lombok {@code @RequiredArgsConstructor}).
     */
    private final ExpenseTransactionService service;

    /**
     * Retrieve all expense transactions for a given user.
     *
     * @param userId UUID of the user whose expense transactions are requested
     * @return HTTP 200 with the list of ExpenseTransaction objects (possibly empty)
     */
    @Operation(summary = "Get all expense transactions for a user", description = "Retrieves all expense transactions associated with a specific user.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExpenseTransaction>> getAllByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.getAllByUser(userId));
    }

    /**
     * Retrieve a specific expense transaction by its identifier.
     *
     * @param id UUID of the expense transaction
     * @return HTTP 200 with the ExpenseTransaction if found, or HTTP 404 if not found
     */
    @Operation(summary = "Get expense transaction by ID", description = "Retrieves a specific expense transaction by its unique identifier.")
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseTransaction> getById(@PathVariable UUID id) {
        Optional<ExpenseTransaction> expense = service.getById(id);
        return expense.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new expense transaction for a specific user.
     *
     * @param userId UUID of the user for whom the expense is created
     * @param request validated request body containing expense details
     * @return HTTP 201 with the newly created ExpenseTransaction
     */
    @Operation(summary = "Create a new expense transaction", description = "Creates a new expense transaction for a specified user.")
    @PostMapping("/user/{userId}")
    public ResponseEntity<ExpenseTransaction> createExpense(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateExpenseRequest request) {

        ExpenseTransaction created = service.createExpenseTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Delete an expense transaction by its identifier.
     *
     * @param id UUID of the expense transaction to delete
     * @return HTTP 204 No Content if deletion succeeds
     */
    @Operation(summary = "Delete an expense transaction", description = "Deletes a specific expense transaction by its unique identifier.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve expense transactions for a user within a time period.
     *
     * @param userId UUID of the user
     * @param start inclusive start Instant for the period (ISO-8601)
     * @param end   inclusive end Instant for the period (ISO-8601)
     * @return HTTP 200 with the list of processed ExpenseTransaction objects in the period
     */
    @Operation(summary = "Get expense transactions by period", description = "Retrieves expense transactions for a user within a specified time period.")
    @GetMapping("/user/{userId}/period")
    public ResponseEntity<List<ExpenseTransaction>> getByPeriod(
            @PathVariable UUID userId,
            @RequestParam Instant start,
            @RequestParam Instant end
    ) {
        return ResponseEntity.ok(service.getProcessedTransactionsByPeriod(userId, start, end));
    }

    /**
     * Count processed expense transactions for a user filtered by status for the given month and year.
     *
     * @param userId UUID of the user
     * @param status ExpenseStatus to filter transactions (e.g., PROCESSED)
     * @param month  month number (1-12)
     * @param year   four-digit year (e.g., 2025)
     * @return HTTP 200 with the count of matching transactions
     */
    @Operation(summary = "Count expense transactions by month", description = "Counts the number of processed expense transactions for a user in a specific month and year, filtered by status.")
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> countByMonth(
            @PathVariable UUID userId,
            @RequestParam ExpenseStatus status,
            @RequestParam int month,
            @RequestParam int year
    ) {
        return ResponseEntity.ok(service.countProcessedTransactionsByMonth(userId, status, month, year));
    }

    /**
     * Retrieve the top merchants where the user saved the most money.
     *
     * @param userId UUID of the user
     * @param limit  maximum number of merchants to return (defaults to 5)
     * @return HTTP 200 with a list of MerchantSavingsDTO ordered by savings descending
     */
    @Operation(summary = "Get top merchants by savings", description = "Retrieves the top merchants where the user has saved the most money.")
    @GetMapping("/user/{userId}/top-merchants")
    public ResponseEntity<List<MerchantSavingsDTO>> getTopMerchants(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(service.getTopMerchants(userId, limit));
    }
}