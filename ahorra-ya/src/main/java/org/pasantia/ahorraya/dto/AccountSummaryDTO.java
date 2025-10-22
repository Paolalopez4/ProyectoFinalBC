package org.pasantia.ahorraya.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data Transfer Object representing a concise summary of a saving account.
 *
 * <p>Used to return account-related summary information to clients, including
 * current balance, total historical savings and relevant timestamps.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountSummaryDTO {

    /**
     * The human-readable or system account number associated with the saving account.
     */
    private String accountNumber;

    /**
     * Current available balance of the saving account.
     *
     * <p>Represented as a BigDecimal to preserve monetary precision.</p>
     */
    private BigDecimal balance;

    /**
     * Total amount historically saved in this account (cumulative).
     *
     * <p>Represents the sum of all successful saving movements.</p>
     */
    private BigDecimal totalHistoricalSavings;

    /**
     * Instant when the last movement affecting the account occurred.
     *
     * <p>ISO-8601 timestamp in UTC.</p>
     */
    private Instant lastMovementAt;

    /**
     * Instant when the account was originally created.
     *
     * <p>ISO-8601 timestamp in UTC.</p>
     */
    private Instant accountCreatedAt;
}
