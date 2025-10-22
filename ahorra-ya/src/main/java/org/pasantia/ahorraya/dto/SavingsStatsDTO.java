package org.pasantia.ahorraya.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object containing aggregated savings statistics.
 *
 * <p>This DTO holds summary metrics derived from saving movements: the total
 * number of movements, the total amount saved, average savings, and the
 * observed maximum and minimum saving amounts. The provided constructor
 * normalizes null inputs to safe defaults (zero values).</p>
 */
@Data
@NoArgsConstructor
public class SavingsStatsDTO {
    /**
     * Total number of saving movements considered in the aggregation.
     *
     * <p>When the source value is null, this field defaults to 0.</p>
     */
    private Long totalMovements;

    /**
     * Total amount saved across all movements.
     *
     * <p>When the source value is null, this field defaults to BigDecimal.ZERO.</p>
     */
    private BigDecimal totalSaved;

    /**
     * Average saving amount per movement.
     *
     * <p>Stored as BigDecimal for monetary precision. When the source value is null,
     * this field defaults to BigDecimal.ZERO.</p>
     */
    private BigDecimal averageSavings;

    /**
     * Maximum single saving amount observed.
     *
     * <p>When the source value is null, this field defaults to BigDecimal.ZERO.</p>
     */
    private BigDecimal maxSavings;

    /**
     * Minimum single saving amount observed.
     *
     * <p>When the source value is null, this field defaults to BigDecimal.ZERO.</p>
     */
    private BigDecimal minSavings;

    /**
     * Constructs a SavingsStatsDTO, converting and normalizing nullable inputs.
     *
     * @param totalMovements total count of saving movements (may be null)
     * @param totalSaved total amount saved (may be null)
     * @param averageSavings average savings expressed as Double (may be null;
     *                        converted to BigDecimal using BigDecimal.valueOf)
     * @param maxSavings maximum saving amount (may be null)
     * @param minSavings minimum saving amount (may be null)
     */
    public SavingsStatsDTO(Long totalMovements,
                           BigDecimal totalSaved,
                           Double averageSavings,
                           BigDecimal maxSavings,
                           BigDecimal minSavings) {
        this.totalMovements = totalMovements != null ? totalMovements : 0L;
        this.totalSaved = totalSaved != null ? totalSaved : BigDecimal.ZERO;
        this.averageSavings = averageSavings != null ? BigDecimal.valueOf(averageSavings) : BigDecimal.ZERO;
        this.maxSavings = maxSavings != null ? maxSavings : BigDecimal.ZERO;
        this.minSavings = minSavings != null ? minSavings : BigDecimal.ZERO;
    }
}
