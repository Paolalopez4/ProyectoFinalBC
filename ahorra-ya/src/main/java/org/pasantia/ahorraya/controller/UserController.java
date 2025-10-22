package org.pasantia.ahorraya.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pasantia.ahorraya.dto.request.CreateUserRequest;
import org.pasantia.ahorraya.dto.TopUserDTO;
import org.pasantia.ahorraya.dto.request.UpdateUserRequest;
import org.pasantia.ahorraya.model.User;
import org.pasantia.ahorraya.model.enums.UserStatus;
import org.pasantia.ahorraya.service.UserService;
import org.pasantia.ahorraya.validation.uservalidations.UserNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller responsible for managing application users.
 *
 * <p>Exposes endpoints to list, create, update, delete and search users, as well as
 * operations to change user status. Endpoints are secured and delegate business logic
 * to {@link UserService}.</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    /**
     * Service that encapsulates user business logic (lookup, creation, update, deletion).
     */
    private final UserService userService;

    /**
     * Password encoder used to hash user passwords before persistence.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieve all users. Requires ADMIN role.
     *
     * @return HTTP 200 with the list of all users.
     */
    @Operation(summary = "Get All Users", description = "Retrieves a list of all users. Admin access required.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Retrieving all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Retrieve a user by their unique identifier. Requires ADMIN role.
     *
     * @param id UUID of the user to retrieve
     * @return HTTP 200 with the user
     */
    @Operation(summary = "Get User by ID", description = "Retrieves a user by their unique identifier. Admin access required.")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        log.info("Retrieving user: {}", id);
        User user = userService.getUserByIdOrThrow(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Create a new user. Requires ADMIN role.
     *
     * @param request validated create user request
     * @return HTTP 201 with the created user
     */
    @Operation(summary = "Create New User", description = "Creates a new user. Admin access required.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating new user: {}", request.getUsername());

        request.setPassword(passwordEncoder.encode(request.getPassword()));

        User created = userService.createUserFromRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing user. Requires ADMIN role.
     *
     * @param id      UUID of the user to update
     * @param request validated update request
     * @return HTTP 200 with the updated user
     */
    @Operation(summary = "Update User", description = "Updates an existing user. Admin access required.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Updating user: {}", id);
        User updated = userService.updateUserFromRequest(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a user by their unique identifier. Requires ADMIN role.
     *
     * @param id UUID of the user to delete
     * @return HTTP 204 No Content
     */
    @Operation(summary = "Delete User", description = "Deletes a user by their unique identifier. Admin access required.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        log.info("Deleting user: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve a paginated list of top savers. Accessible by users and admins.
     *
     * @param page page index (0-based)
     * @param size page size
     * @return HTTP 200 with list of top savers
     */
    @Operation(summary = "Get Top Savers", description = "Retrieves a paginated list of top savers. Accessible by users and admins.")
    @GetMapping("/top-savers")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TopUserDTO>> getTopSavers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.info("Retrieving top savers - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.getTopSavers(pageable));
    }

    /**
     * Count users by status. Requires ADMIN role.
     *
     * @param status user status to count
     * @return HTTP 200 with the number of users with the given status
     */
    @Operation(summary = "Count Users by Status", description = "Counts the number of users with a specific status. Admin access required.")
    @GetMapping("/count-by-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> countByStatus(@RequestParam UserStatus status) {
        log.info("Counting users with status: {}", status);
        return ResponseEntity.ok(userService.countByStatus(status));
    }

    /**
     * Search for a user by username, email, or identification number. Requires ADMIN role.
     *
     * <p>At least one of {@code username}, {@code email} or {@code identification} must be provided.</p>
     *
     * @param username       optional username to search
     * @param email          optional email to search
     * @param identification optional identification number to search
     * @return HTTP 200 with the found user
     * @throws IllegalArgumentException if no search parameter is provided
     * @throws UserNotFoundException    if no matching user is found
     */
    @Operation(summary = "Search User", description = "Searches for a user by username, email, or identification number. Admin access required.")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> searchUser(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String identification) {

        log.info("Searching user - username: {}, email: {}, identification: {}",
                username, email, identification);

        if (username != null) {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
            return ResponseEntity.ok(user);
        }

        if (email != null) {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
            return ResponseEntity.ok(user);
        }

        if (identification != null) {
            User user = userService.findByIdentificationNumber(identification)
                    .orElseThrow(() -> new UserNotFoundException("User not found with identification: " + identification));
            return ResponseEntity.ok(user);
        }

        throw new IllegalArgumentException("At least one search parameter must be provided");
    }

    /**
     * Activate a user account. Requires ADMIN role.
     *
     * @param id UUID of the user to activate
     * @return HTTP 200 with the activated user
     */
    @Operation(summary = "Activate User", description = "Activates a user account. Admin access required.")
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> activateUser(@PathVariable UUID id) {
        log.info("Activating user: {}", id);
        User user = userService.getUserByIdOrThrow(id);
        user.activate();
        userService.updateUser(id, user);
        return ResponseEntity.ok(user);
    }

    /**
     * Deactivate a user account. Requires ADMIN role.
     *
     * @param id UUID of the user to deactivate
     * @return HTTP 200 with the deactivated user
     */
    @Operation(summary = "Deactivate User", description = "Deactivates a user account. Admin access required.")
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> deactivateUser(@PathVariable UUID id) {
        log.info("Deactivating user: {}", id);
        User user = userService.getUserByIdOrThrow(id);
        user.deactivate();
        userService.updateUser(id, user);
        return ResponseEntity.ok(user);
    }

    /**
     * Suspend a user account. Requires ADMIN role.
     *
     * @param id UUID of the user to suspend
     * @return HTTP 200 with the suspended user
     */
    @Operation(summary = "Suspend User", description = "Suspends a user account. Admin access required.")
    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> suspendUser(@PathVariable UUID id) {
        log.info("Suspending user: {}", id);
        User user = userService.getUserByIdOrThrow(id);
        user.suspend();
        userService.updateUser(id, user);
        return ResponseEntity.ok(user);
    }
}