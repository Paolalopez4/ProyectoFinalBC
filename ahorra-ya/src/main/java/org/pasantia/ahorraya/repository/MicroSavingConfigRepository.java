package org.pasantia.ahorraya.repository;

import org.pasantia.ahorraya.model.MicroSavingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing micro-saving configurations.
 */
@Repository
public interface MicroSavingConfigRepository extends JpaRepository<MicroSavingConfig, UUID> {
    /**
     * Finds the active micro-saving configuration for a user.
     *
     * @param userId the user's unique identifier
     * @return an Optional containing the active configuration if found
     */
    Optional<MicroSavingConfig> findByUserIdAndActiveTrue(UUID userId);

    /**
     * Checks if a user has an active micro-saving configuration.
     *
     * @param userId the user's unique identifier
     * @return true if an active configuration exists
     */
    boolean existsByUserIdAndActiveTrue(UUID userId);
}
