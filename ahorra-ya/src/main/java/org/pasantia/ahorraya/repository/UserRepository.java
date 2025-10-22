package org.pasantia.ahorraya.repository;

import org.pasantia.ahorraya.dto.TopUserDTO;
import org.pasantia.ahorraya.model.User;
import org.pasantia.ahorraya.model.enums.SavingAccountStatus;
import org.pasantia.ahorraya.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for CRUD operations and queries on {@link User}.
 * Provides lookup by username, email, identification, top savers ranking, and status counts.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Finds a user by username.
     *
     * @param username the username
     * @return an {@code Optional} containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by email.
     *
     * @param email the email address
     * @return an {@code Optional} containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by identification number.
     *
     * @param identificationNumber the identification number
     * @return an {@code Optional} containing the user if found
     */
    Optional<User> findByIdentificationNumber(String identificationNumber);

    /**
     * Retrieves top savers ranked by current balance.
     * Includes users with active saving accounts and their movement count.
     *
     * @param pageable pagination and sorting parameters
     * @return list of {@link TopUserDTO} ordered by balance descending
     */
    @Query("SELECT new org.pasantia.ahorraya.dto.TopUserDTO(" +
            "u.id, " +
            "u.firstName, " +
            "u.lastName, " +
            "u.email, " +
            "sa.balance, " +
            "sa.totalHistoricalSavings, " +
            "CAST((SELECT COUNT(sm) FROM SavingMovement sm WHERE sm.user.id = u.id) AS long)) " +
            "FROM User u " +
            "JOIN SavingAccount sa ON sa.user.id = u.id " +
            "WHERE sa.status = 'ACTIVE' " +
            "ORDER BY sa.balance DESC")
    List<TopUserDTO> getTopSavers(Pageable pageable);

    /**
     * Counts users by status.
     *
     * @param userStatus the user status
     * @return total number of users with the given status
     */
    Long countByStatus(UserStatus userStatus);
}
