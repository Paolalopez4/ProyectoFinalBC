package org.pasantia.ahorraya.repository;

import org.pasantia.ahorraya.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
/**
 * Repository interface for Admin entities.
 *
 * <p>Provides CRUD and lookup operations for Admin objects persisted in the database.
 * This interface extends Spring Data JPA's {@link JpaRepository} to inherit common
 * persistence methods and also declares domain-specific lookup methods.</p>
 */
public interface AdminRepository extends JpaRepository<Admin, UUID> {

    /**
     * Find an administrator by their email address.
     *
     * @param email the email address to search for (case-sensitive matching depends on DB collation)
     * @return an Optional containing the Admin if found, or an empty Optional otherwise
     */
    Optional<Admin> findByEmail(String email);

    /**
     * Check whether an administrator exists with the given email address.
     *
     * @param email the email address to check
     * @return true if an Admin with the provided email exists, false otherwise
     */
    boolean existsByEmail(String email);
}