package org.pasantia.ahorraya.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO that summarizes top saving users for leaderboard displays.
 *
 * <p>Provides basic identity information and aggregated savings metrics used
 * to rank or display top savers in the application.</p>
 */
@Data
@NoArgsConstructor
public class TopUserDTO {
    /**
     * Unique identifier of the user.
     */
    private UUID id;

    /**
     * User's first name.
     */
    private String firstName;

    /**
     * User's last name.
     */
    private String lastName;

    /**
     * User's email address.
     */
    private String email;

    /**
     * Current account balance for the user.
     *
     * <p>Defaults to BigDecimal.ZERO when null in the constructor.</p>
     */
    private BigDecimal balance;

    /**
     * Total historical savings accumulated by the user.
     *
     * <p>Defaults to BigDecimal.ZERO when null in the constructor.</p>
     */
    private BigDecimal totalHistoricalSavings;

    /**
     * Number of saving movements counted for this user.
     *
     * <p>Defaults to 0 when null in the constructor.</p>
     */
    private Long savingsMovementsCount;

    /**
     * Constructs a TopUserDTO and normalizes nullable numeric fields.
     *
     * @param id                     unique user id
     * @param firstName              user's first name
     * @param lastName               user's last name
     * @param email                  user's email address
     * @param balance                current balance (if null, treated as BigDecimal.ZERO)
     * @param totalHistoricalSavings total historical savings (if null, treated as BigDecimal.ZERO)
     * @param savingsMovementsCount  number of saving movements (if null, treated as 0)
     */
    public TopUserDTO(UUID id,
                      String firstName,
                      String lastName,
                      String email,
                      BigDecimal balance,
                      BigDecimal totalHistoricalSavings,
                      Long savingsMovementsCount) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.totalHistoricalSavings = totalHistoricalSavings != null ? totalHistoricalSavings : BigDecimal.ZERO;
        this.savingsMovementsCount = savingsMovementsCount != null ? savingsMovementsCount : 0L;
    }
}