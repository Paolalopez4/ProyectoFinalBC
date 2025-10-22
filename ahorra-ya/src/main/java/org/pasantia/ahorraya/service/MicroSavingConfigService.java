package org.pasantia.ahorraya.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pasantia.ahorraya.model.MicroSavingConfig;
import org.pasantia.ahorraya.model.User;
import org.pasantia.ahorraya.repository.MicroSavingConfigRepository;
import org.pasantia.ahorraya.repository.UserRepository;
import org.pasantia.ahorraya.validation.BusinessException;
import org.pasantia.ahorraya.validation.microsavingconfigvalidations.ConfigAlreadyActiveException;
import org.pasantia.ahorraya.validation.microsavingconfigvalidations.MicroSavingConfigNotFoundException;
import org.pasantia.ahorraya.validation.uservalidations.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for managing micro-saving configurations for users.
 *
 * <p>Provides operations to create a default configuration, activate and deactivate
 * existing configurations, and to retrieve the active configuration for a user.
 * All persistence operations are delegated to the configured repositories.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MicroSavingConfigService {

    /**
     * Repository for MicroSavingConfig persistence and queries.
     */
    private final MicroSavingConfigRepository repository;

    /**
     * Repository used to lookup users by id.
     */
    private final UserRepository userRepository;

    /**
     * Create a default micro-saving configuration for the given user.
     *
     * <p>If the user already has an active configuration a {@link ConfigAlreadyActiveException}
     * is thrown. The user must exist or a {@link UserNotFoundException} is thrown.</p>
     *
     * @param userId the user's UUID
     * @return the persisted MicroSavingConfig
     */
    @Transactional
    public MicroSavingConfig createDefaultConfig(UUID userId) {
        log.info("Creating micro-saving configuration for user: {}", userId);

        if (repository.existsByUserIdAndActiveTrue(userId)) {
            log.warn("User {} already has an active configuration", userId);
            throw new ConfigAlreadyActiveException(userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        MicroSavingConfig config = new MicroSavingConfig(user, true, 1);
        MicroSavingConfig saved = repository.save(config);

        log.info("Micro-saving configuration created for user: {}", userId);
        return saved;
    }

    /**
     * Activate the micro-saving configuration identified by {@code configId}.
     *
     * <p>If the configuration is already active a {@link BusinessException} is thrown.
     * If the configuration does not exist a {@link MicroSavingConfigNotFoundException} is thrown.</p>
     *
     * @param configId UUID of the configuration to activate
     * @return the updated MicroSavingConfig after activation
     */
    @Transactional
    public MicroSavingConfig activate(UUID configId) {
        MicroSavingConfig config = repository.findById(configId)
                .orElseThrow(() -> new MicroSavingConfigNotFoundException(configId));

        if (config.isActive()) {
            log.warn("Attempt to activate an already active configuration: {}", configId);
            throw new BusinessException("Configuration is already active");
        }

        config.activateConfig();
        MicroSavingConfig saved = repository.save(config);

        log.info("Configuration activated: {}", configId);
        return saved;
    }

    /**
     * Deactivate the micro-saving configuration identified by {@code configId}.
     *
     * <p>If the configuration is already inactive a {@link BusinessException} is thrown.
     * If the configuration does not exist a {@link MicroSavingConfigNotFoundException} is thrown.</p>
     *
     * @param configId UUID of the configuration to deactivate
     * @return the updated MicroSavingConfig after deactivation
     */
    @Transactional
    public MicroSavingConfig deactivate(UUID configId) {
        MicroSavingConfig config = repository.findById(configId)
                .orElseThrow(() -> new MicroSavingConfigNotFoundException(configId));

        if (!config.isActive()) {
            log.warn("Attempt to deactivate an already inactive configuration: {}", configId);
            throw new BusinessException("Configuration is already inactive");
        }

        config.deactivateConfig();
        MicroSavingConfig saved = repository.save(config);

        log.info("Configuration deactivated: {}", configId);
        return saved;
    }

    /**
     * Retrieve the active micro-saving configuration for the specified user, if any.
     *
     * @param userId the user's UUID
     * @return Optional containing the active MicroSavingConfig or empty if none exists
     */
    public Optional<MicroSavingConfig> getActiveConfigByUser(UUID userId) {
        return repository.findByUserIdAndActiveTrue(userId);
    }
}