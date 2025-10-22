package org.pasantia.ahorraya.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pasantia.ahorraya.model.Admin;
import org.pasantia.ahorraya.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller that exposes endpoints to manage administrator accounts.
 *
 * <p>Provides CRUD and utility endpoints for Admin entities. All endpoints in this
 * controller require ADMIN role. Logging is used to trace operations.</p>
 */
@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admins", description = "Admin management endpoints")
public class AdminController {

    /**
     * Service that encapsulates business logic for Admin operations.
     */
    private final AdminService adminService;

    /**
     * Password encoder used to hash plain-text passwords before persisting.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieve a list of all administrators.
     *
     * @return ResponseEntity containing the list of all Admin objects (HTTP 200).
     */
    @Operation(summary = "Get all admins", description = "Retrieve a list of all administrators.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Admin>> getAllAdmins() {
        log.info("Retrieving all administrators");
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    /**
     * Retrieve a list of active administrators.
     *
     * @return ResponseEntity containing the list of active Admin objects (HTTP 200).
     */
    @Operation(summary = "Get active admins", description = "Retrieve a list of all active administrators.")
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Admin>> getActiveAdmins() {
        log.info("Retrieving active administrators");
        return ResponseEntity.ok(adminService.getActiveAdmins());
    }

    /**
     * Retrieve an administrator by their unique identifier.
     *
     * @param id UUID of the administrator to retrieve.
     * @return ResponseEntity containing the Admin object (HTTP 200).
     */
    @Operation(summary = "Get admin by ID", description = "Retrieve an administrator by their unique ID.")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Admin> getAdminById(@PathVariable UUID id) {
        log.info("Retrieving administrator: {}", id);
        Admin admin = adminService.getAdminByIdOrThrow(id);
        return ResponseEntity.ok(admin);
    }

    /**
     * Retrieve an administrator by their email address.
     *
     * @param email Email address of the administrator to retrieve.
     * @return ResponseEntity containing the Admin object (HTTP 200).
     */
    @Operation(summary = "Get admin by email", description = "Retrieve an administrator by their email address.")
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Admin> getAdminByEmail(@PathVariable String email) {
        log.info("Retrieving administrator by email: {}", email);
        Admin admin = adminService.findByEmailOrThrow(email);
        return ResponseEntity.ok(admin);
    }

    /**
     * Create a new administrator account. If a plain-text password is supplied it will be encoded.
     *
     * @param admin Admin object to create (validated).
     * @return ResponseEntity containing the created Admin (HTTP 201).
     */
    @Operation(summary = "Create new admin", description = "Create a new administrator account.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Admin> createAdmin(@Valid @RequestBody Admin admin) {
        log.info("Creating new administrator: {}", admin.getEmail());

        // Encrypt password if provided in plain text
        if (admin.getPassword() != null && !admin.getPassword().startsWith("$2a$")) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        }

        Admin created = adminService.createAdmin(admin);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update details of an existing administrator.
     *
     * @param id    UUID of the administrator to update.
     * @param admin Admin object with updated fields (validated).
     * @return ResponseEntity containing the updated Admin (HTTP 200).
     */
    @Operation(summary = "Update admin", description = "Update an existing administrator's details.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Admin> updateAdmin(
            @PathVariable UUID id,
            @Valid @RequestBody Admin admin) {

        log.info("Updating administrator: {}", id);
        Admin updated = adminService.updateAdmin(id, admin);
        return ResponseEntity.ok(updated);
    }

    /**
     * Activate (enable) an administrator account.
     *
     * @param id UUID of the administrator to activate.
     * @return ResponseEntity containing the activated Admin (HTTP 200).
     */
    @Operation(summary = "Activate admin", description = "Activate an administrator account.")
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Admin> activateAdmin(@PathVariable UUID id) {
        log.info("Activating administrator: {}", id);
        Admin activated = adminService.activateAdmin(id);
        return ResponseEntity.ok(activated);
    }

    /**
     * Deactivate (disable) an administrator account.
     *
     * @param id UUID of the administrator to deactivate.
     * @return ResponseEntity containing the deactivated Admin (HTTP 200).
     */
    @Operation(summary = "Deactivate admin", description = "Deactivate an administrator account.")
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Admin> deactivateAdmin(@PathVariable UUID id) {
        log.info("Deactivating administrator: {}", id);
        Admin deactivated = adminService.deactivateAdmin(id);
        return ResponseEntity.ok(deactivated);
    }

    /**
     * Soft delete (deactivate) an administrator account.
     *
     * @param id UUID of the administrator to soft delete.
     * @return ResponseEntity with no content (HTTP 204).
     */
    @Operation(summary = "Delete admin", description = "Soft delete (deactivate) an administrator account.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAdmin(@PathVariable UUID id) {
        log.info("Deleting (deactivating) administrator: {}", id);
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Permanently delete an administrator account.
     *
     * @param id UUID of the administrator to permanently delete.
     * @return ResponseEntity with no content (HTTP 204).
     */
    @Operation(summary = "Permanently delete admin", description = "Permanently delete an administrator account.")
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> permanentlyDeleteAdmin(@PathVariable UUID id) {
        log.warn("Permanent deletion of administrator: {}", id);
        adminService.permanentlyDeleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Count active administrators.
     *
     * @return ResponseEntity containing the number of active admins (HTTP 200).
     */
    @Operation(summary = "Count active admins", description = "Get the count of active administrators.")
    @GetMapping("/count/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> countActiveAdmins() {
        log.info("Counting active administrators");
        return ResponseEntity.ok(adminService.countActiveAdmins());
    }
}
