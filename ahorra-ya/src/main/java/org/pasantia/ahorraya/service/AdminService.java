package org.pasantia.ahorraya.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pasantia.ahorraya.model.Admin;
import org.pasantia.ahorraya.repository.AdminRepository;
import org.pasantia.ahorraya.validation.adminvalidations.AdminNotFoundException;
import org.pasantia.ahorraya.validation.uservalidations.DuplicateEmailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service that encapsulates business logic for administrator management.
 *
 * <p>Provides methods to create, update, activate/deactivate, and remove administrators.
 * Transactional boundaries are applied where updates occur. Logging and validation checks
 * are performed to prevent duplicate emails and to handle not-found cases.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminService {

    /**
     * Repository that provides persistence operations for Admin entities.
     */
    private final AdminRepository adminRepository;

    /**
     * Retrieve all administrators.
     *
     * @return list of all Admin entities
     */
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    /**
     * Retrieve an administrator by ID or throw AdminNotFoundException if not found.
     *
     * @param id UUID of the admin
     * @return Admin entity
     * @throws AdminNotFoundException when no admin exists with the provided id
     */
    public Admin getAdminByIdOrThrow(UUID id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new AdminNotFoundException(id));
    }

    /**
     * Find an administrator by email.
     *
     * @param email email to search (case-insensitive handled by caller)
     * @return Optional containing the Admin if found, otherwise empty
     */
    public Optional<Admin> findByEmail(String email) {
        return adminRepository.findByEmail(email.toLowerCase());
    }

    /**
     * Find an administrator by email or throw AdminNotFoundException if not found.
     *
     * @param email email to search
     * @return Admin entity
     * @throws AdminNotFoundException when no admin exists with the provided email
     */
    public Admin findByEmailOrThrow(String email) {
        return adminRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new AdminNotFoundException("Admin not found with email: " + email));
    }

    /**
     * Create a new administrator.
     *
     * <p>Validates duplicate email before persisting.</p>
     *
     * @param admin Admin entity to create
     * @return persisted Admin entity
     * @throws DuplicateEmailException when the email is already registered
     */
    @Transactional
    public Admin createAdmin(Admin admin) {
        log.info("Creating admin with email: {}", admin.getEmail());

        if (adminRepository.existsByEmail(admin.getEmail())) {
            log.warn("Attempt to create admin with already registered email: {}", admin.getEmail());
            throw new DuplicateEmailException(admin.getEmail());
        }

        Admin savedAdmin = adminRepository.save(admin);
        log.info("Admin created successfully with ID: {}", savedAdmin.getId());

        return savedAdmin;
    }

    /**
     * Update an existing administrator.
     *
     * <p>If the email is changed, checks for duplicates before updating.</p>
     *
     * @param id           UUID of the admin to update
     * @param updatedAdmin Admin object carrying updated values
     * @return updated Admin entity
     * @throws AdminNotFoundException when the admin to update does not exist
     * @throws DuplicateEmailException when the new email is already in use by another admin
     */
    @Transactional
    public Admin updateAdmin(UUID id, Admin updatedAdmin) {
        Admin existingAdmin = adminRepository.findById(id)
                .orElseThrow(() -> new AdminNotFoundException(id));

        if (!existingAdmin.getEmail().equals(updatedAdmin.getEmail())) {
            if (adminRepository.existsByEmail(updatedAdmin.getEmail())) {
                log.warn("Attempt to update to an existing email: {}", updatedAdmin.getEmail());
                throw new DuplicateEmailException(updatedAdmin.getEmail());
            }
        }

        existingAdmin.setEmail(updatedAdmin.getEmail());
        existingAdmin.setActive(updatedAdmin.isActive());

        log.info("Admin updated: {}", id);
        return adminRepository.save(existingAdmin);
    }

    /**
     * Activate an administrator.
     *
     * @param id UUID of the admin to activate
     * @return activated Admin entity
     * @throws AdminNotFoundException when the admin does not exist
     * @throws IllegalStateException  when the admin is already active
     */
    @Transactional
    public Admin activateAdmin(UUID id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AdminNotFoundException(id));

        if (admin.isActive()) {
            log.warn("Attempt to activate admin already active: {}", id);
            throw new IllegalStateException("The administrator is already active");
        }

        admin.setActive(true);
        adminRepository.save(admin);

        log.info("Admin activated: {}", admin.getEmail());
        return admin;
    }

    /**
     * Deactivate an administrator.
     *
     * @param id UUID of the admin to deactivate
     * @return deactivated Admin entity
     * @throws AdminNotFoundException when the admin does not exist
     * @throws IllegalStateException  when the admin is already inactive
     */
    @Transactional
    public Admin deactivateAdmin(UUID id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AdminNotFoundException(id));

        if (!admin.isActive()) {
            log.warn("Attempt to deactivate admin already inactive: {}", id);
            throw new IllegalStateException("The administrator is already inactive");
        }

        admin.setActive(false);
        adminRepository.save(admin);

        log.info("Admin deactivated: {}", admin.getEmail());
        return admin;
    }

    /**
     * Delete an administrator (soft delete by setting inactive).
     *
     * @param id UUID of the admin to soft-delete
     * @throws AdminNotFoundException when the admin does not exist
     */
    @Transactional
    public void deleteAdmin(UUID id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AdminNotFoundException(id));

        admin.setActive(false);
        adminRepository.save(admin);

        log.info("Admin deleted (deactivated): {}", admin.getEmail());
    }

    /**
     * Permanently delete an administrator (hard delete).
     *
     * @param id UUID of the admin to delete permanently
     * @throws AdminNotFoundException when the admin does not exist
     */
    @Transactional
    public void permanentlyDeleteAdmin(UUID id) {
        if (!adminRepository.existsById(id)) {
            throw new AdminNotFoundException(id);
        }

        adminRepository.deleteById(id);
        log.warn("Admin permanently deleted: {}", id);
    }

    /**
     * Count active administrators.
     *
     * @return number of admins with active == true
     */
    public long countActiveAdmins() {
        return adminRepository.findAll().stream()
                .filter(Admin::isActive)
                .count();
    }

    /**
     * Retrieve only active administrators.
     *
     * @return list of active Admin entities
     */
    public List<Admin> getActiveAdmins() {
        return adminRepository.findAll().stream()
                .filter(Admin::isActive)
                .toList();
    }
}
