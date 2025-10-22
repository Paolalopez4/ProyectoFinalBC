package org.pasantia.ahorraya.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object that summarizes savings associated with a merchant.
 *
 * <p>Provides aggregated information about how much a user (or group of users)
 * saved when transacting with a specific merchant: the merchant name, number of
 * qualifying transactions, total amount saved and the average saving per transaction.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantSavingsDTO {

    /**
     * Merchant name or identifier where the transactions occurred.
     */
    private String merchant;

    /**
     * Number of transactions considered in the aggregation.
     *
     * <p>Represents the count of transactions that contributed to the savings total.</p>
     */
    private Long transactionCount;

    /**
     * Total amount saved at this merchant.
     *
     * <p>Monetary amount represented as BigDecimal to preserve precision.</p>
     */
    private BigDecimal totalSaved;

    /**
     * Average savings per transaction for this merchant.
     *
     * <p>Expressed as a double, typically in the same currency as {@code totalSaved} divided by {@code transactionCount}.</p>
     */
    private Double averageSavings;
}
