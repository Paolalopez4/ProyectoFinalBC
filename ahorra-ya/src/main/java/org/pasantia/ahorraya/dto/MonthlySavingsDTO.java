package org.pasantia.ahorraya.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object representing aggregated savings statistics for a specific month.
 *
 * <p>Used to communicate monthly savings metrics such as total saved amount,
 * number of qualifying transactions and average saving per transaction.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySavingsDTO {

    /**
     * Month number (1-12) that this record corresponds to.
     */
    private Integer month;

    /**
     * Four-digit year that this record corresponds to (for example: 2025).
     */
    private Integer year;

    /**
     * Total amount saved during the month.
     *
     * <p>Monetary amount represented as BigDecimal to preserve precision.</p>
     */
    private BigDecimal totalSaved;

    /**
     * Number of transactions considered in the aggregation.
     */
    private Long transactionCount;

    /**
     * Average saving amount per transaction for the month.
     *
     * <p>Represented as BigDecimal to preserve monetary precision.</p>
     */
    private BigDecimal averageSavings;
}
