package org.pasantia.ahorraya.repository;

import org.pasantia.ahorraya.dto.AccountSummaryDTO;
import org.pasantia.ahorraya.model.SavingAccount;
import org.pasantia.ahorraya.model.enums.SavingAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavingAccountRepository extends JpaRepository<SavingAccount, UUID> {
    /**
     * Finds a saving account by user ID.
     */
    Optional<SavingAccount> findByUserId(UUID userId);

    /**
     * Finds a saving account by account number.
     */
    Optional<SavingAccount> findByAccountNumber(String accountNumber);

    /**
     * Retrieves a summary of the user's saving account.
     *
     * @param userId the user's unique identifier
     * @return a DTO containing account balance, total savings, and timestamps
     */
    /**
     * Retrieves a summary of the user's saving account.
     */
    @Query("SELECT new org.pasantia.ahorraya.dto.AccountSummaryDTO(" +
            "sa.accountNumber, " +
            "sa.balance, " +
            "sa.totalHistoricalSavings, " +
            "sa.lastMovementAt, " +
            "sa.audit.createdAt) " +
            "FROM SavingAccount sa " +
            "WHERE sa.user.id = :userId")
    AccountSummaryDTO getAccountSummary(@Param("userId") UUID userId);

    /**
     * Checks if a user has an account with a specific status.
     */
    boolean existsByUserIdAndStatusAndIsDeletedFalse(UUID userId, SavingAccountStatus status);
}
