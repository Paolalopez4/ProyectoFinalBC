package org.pasantia.ahorraya.repository;

import org.pasantia.ahorraya.dto.MonthlySavingsDTO;
import org.pasantia.ahorraya.dto.SavingsStatsDTO;
import org.pasantia.ahorraya.model.SavingMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for CRUD operations and aggregate queries over {@link SavingMovement}.
 * Provides monthly evolution, overall stats, and range-based totals for a user's
 * credit movements.
 */
@Repository
public interface SavingMovementRepository extends JpaRepository<SavingMovement, UUID> {
    /**
     * Retrieves monthly savings evolution for a user.
     *
     * @param userId user ID
     * @return list of monthly statistics ordered by year and month descending
     */
    @Query("SELECT MONTH(sm.audit.createdAt), YEAR(sm.audit.createdAt), SUM(sm.savingsAmount), COUNT(sm.id), AVG(sm.savingsAmount) " +
            "FROM SavingMovement sm " +
            "WHERE sm.user.id = :userId AND sm.movementType = 'CREDIT' " +
            "GROUP BY YEAR(sm.audit.createdAt), MONTH(sm.audit.createdAt) " +
            "ORDER BY YEAR(sm.audit.createdAt) DESC, MONTH(sm.audit.createdAt) DESC")
    List<Object[]> getMonthlySavingsEvolution(@Param("userId") UUID userId);

    /**
     * Retrieves overall savings statistics for a user.
     * Aggregate values may be {@code null} if no data exists.
     *
     * @param userId user ID
     * @return statistics with count, sum, average, max, and min
     */
    @Query("SELECT new org.pasantia.ahorraya.dto.SavingsStatsDTO(" +
            "COUNT(sm.id), " +
            "SUM(sm.savingsAmount), " +
            "AVG(sm.savingsAmount), " +
            "MAX(sm.savingsAmount), " +
            "MIN(sm.savingsAmount)) " +
            "FROM SavingMovement sm " +
            "WHERE sm.user.id = :userId " +
            "AND sm.movementType = 'CREDIT'")
    SavingsStatsDTO getUserSavingsStats(@Param("userId") UUID userId);

    /**
     * Retrieves total savings for a specific month and year.
     *
     * @param userId user ID
     * @param month month (1-12)
     * @param year year (yyyy)
     * @return total saved or {@code null} if no data
     */
    @Query("SELECT SUM(sm.savingsAmount) " +
            "FROM SavingMovement sm " +
            "WHERE sm.user.id = :userId " +
            "AND sm.movementType = 'CREDIT' " +
            "AND MONTH(sm.audit.createdAt) = :month " +
            "AND YEAR(sm.audit.createdAt) = :year")
    BigDecimal getSavingsByMonth(
            @Param("userId") UUID userId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    /**
     * Retrieves total savings in a date range (inclusive).
     *
     * @param userId user ID
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return total saved, returns 0 if no data
     */
    @Query("SELECT COALESCE(SUM(sm.savingsAmount), 0) " +
            "FROM SavingMovement sm " +
            "WHERE sm.user.id = :userId " +
            "AND sm.movementType = 'CREDIT' " +
            "AND sm.movementDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSavingsInRange(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Counts all saving movements for a user.
     *
     * @param userId user ID
     * @return total number of movements
     */
    Long countByUserId(UUID userId);
}
