package org.pasantia.ahorraya.repository;

import org.pasantia.ahorraya.dto.MerchantSavingsDTO;
import org.pasantia.ahorraya.model.ExpenseTransaction;
import org.pasantia.ahorraya.model.enums.ExpenseStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
/**
 * Repository for performing CRUD and domain-specific queries on ExpenseTransaction entities.
 *
 * <p>This interface extends {@link JpaRepository} to provide standard persistence
 * operations and declares custom queries used by the application to retrieve
 * processed transactions, count transactions by month and obtain merchant-based
 * savings aggregations.</p>
 */
public interface ExpenseTransactionRepository extends JpaRepository<ExpenseTransaction, UUID> {
    /**
     * Retrieves all processed transactions for a user within a specified date range.
     *
     * <p>Returns transactions with status PROCESSED belonging to the given user,
     * whose transactionDate falls between the provided startDate and endDate.
     * Results are ordered by transaction date in descending order.</p>
     *
     * @param userId    the user's unique identifier
     * @param startDate inclusive start instant of the date range
     * @param endDate   inclusive end instant of the date range
     * @return list of processed ExpenseTransaction entities ordered by transactionDate descending
     */
    @Query("SELECT et FROM ExpenseTransaction et " +
            "WHERE et.user.id = :userId " +
            "AND et.status = 'PROCESSED' " +
            "AND et.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY et.transactionDate DESC")
    List<ExpenseTransaction> getTransactionsByPeriod(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );


    /**
     * Counts the number of transactions for a user with the specified status in a given month and year.
     *
     * <p>The query filters by user id, the provided {@code status} and the month/year
     * extracted from the transactionDate.</p>
     *
     * @param userId the user's unique identifier
     * @param status the transaction status to filter by (e.g., PROCESSED)
     * @param month  the month number (1-12)
     * @param year   the four-digit year (for example: 2024)
     * @return the count of ExpenseTransaction records that match the criteria
     */
    @Query("SELECT COUNT(et.id) FROM ExpenseTransaction et " +
            "WHERE et.user.id = :userId " +
            "AND et.status = :status " +
            "AND MONTH(et.transactionDate) = :month " +
            "AND YEAR(et.transactionDate) = :year")
    Long countTransactionsByMonth(
            @Param("userId") UUID userId,
            @Param("status") ExpenseStatus status,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    /**
     * Retrieves merchant-level savings aggregates for a user, ordered by total savings descending.
     *
     * <p>Returns a list of {@link MerchantSavingsDTO} with the merchant name, number
     * of qualifying transactions, total saved amount and average saved amount per merchant.
     * Only processed transactions with non-null merchant are considered.</p>
     *
     * @param userId   the user's unique identifier
     * @param pageable pagination information (limit and offset are honored)
     * @return list of MerchantSavingsDTO ordered by total saved amount descending
     */
    @Query("SELECT new org.pasantia.ahorraya.dto.MerchantSavingsDTO(" +
            "et.merchant, " +
            "COUNT(et.id), " +
            "SUM(et.savingsDifference), " +
            "AVG(et.savingsDifference)) " +
            "FROM ExpenseTransaction et " +
            "WHERE et.user.id = :userId " +
            "AND et.status = 'PROCESSED' " +
            "AND et.merchant IS NOT NULL " +
            "GROUP BY et.merchant " +
            "ORDER BY SUM(et.savingsDifference) DESC")
    List<MerchantSavingsDTO> getTopMerchants(@Param("userId") UUID userId, Pageable pageable);
}
