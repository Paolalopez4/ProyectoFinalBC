package org.pasantia.ahorraya.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pasantia.ahorraya.dto.request.CreateUserRequest;
import org.pasantia.ahorraya.dto.TopUserDTO;
import org.pasantia.ahorraya.dto.request.UpdateUserRequest;
import org.pasantia.ahorraya.model.SavingAccount;
import org.pasantia.ahorraya.model.User;
import org.pasantia.ahorraya.model.enums.SavingAccountStatus;
import org.pasantia.ahorraya.model.enums.UserStatus;
import org.pasantia.ahorraya.repository.UserRepository;
import org.pasantia.ahorraya.validation.BusinessException;
import org.pasantia.ahorraya.validation.uservalidations.DuplicateEmailException;
import org.pasantia.ahorraya.validation.uservalidations.DuplicateIdentificationException;
import org.pasantia.ahorraya.validation.uservalidations.DuplicateUsernameException;
import org.pasantia.ahorraya.validation.uservalidations.UserNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service that manages application users.
 *
 * <p>Provides methods to list, create, update and remove users, and to perform
 * common lookups. Business validation (duplicate checks, account creation) and
 * basic logging are performed here. Transactional boundaries are applied for
 * mutating operations.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * Retrieve all users.
     *
     * @return a list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    /**
     * Retrieve a user by id or throw UserNotFoundException if not present.
     *
     * @param id user UUID
     * @return the found User
     * @throws UserNotFoundException when the user does not exist
     */
    public User getUserByIdOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Create a new user from the provided request DTO.
     *
     * <p>Performs duplicate checks for username, email and identification number,
     * creates a default saving account for the user and persists the entity.</p>
     *
     * @param request DTO containing the user creation data
     * @return the persisted User
     * @throws DuplicateUsernameException       when username already exists
     * @throws DuplicateEmailException          when email already exists
     * @throws DuplicateIdentificationException when identification number already exists
     */
    @Transactional
    public User createUserFromRequest(CreateUserRequest request) {
        log.info("Creating user from request: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateUsernameException(request.getUsername());
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException(request.getEmail());
        }

        if (userRepository.findByIdentificationNumber(request.getIdentificationNumber()).isPresent()) {
            throw new DuplicateIdentificationException(request.getIdentificationNumber());
        }

        User user = new User(
                request.getIdentificationNumber(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhone(),
                request.getUsername(),
                request.getPassword(),
                request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE
        );

        SavingAccount account = new SavingAccount(
                user,
                BigDecimal.ZERO,
                SavingAccountStatus.ACTIVE,
                BigDecimal.ZERO
        );
        account.setUser(user);
        user.getAccounts().add(account);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * Update an existing user from the provided DTO.
     *
     * <p>Performs duplicate checks when email or username change, and applies
     * provided updates to mutable fields.</p>
     *
     * @param id      UUID of the user to update
     * @param request DTO containing fields to update
     * @return the updated User
     * @throws UserNotFoundException            when the user does not exist
     * @throws DuplicateEmailException          when the new email is already in use
     * @throws DuplicateUsernameException       when the new username is already in use
     */
    @Transactional
    public User updateUserFromRequest(UUID id, UpdateUserRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (request.getEmail() != null && !existingUser.getEmail().equals(request.getEmail())) {
            userRepository.findByEmail(request.getEmail())
                    .ifPresent(u -> {
                        throw new DuplicateEmailException(request.getEmail());
                    });
        }

        if (request.getUsername() != null && !existingUser.getUsername().equals(request.getUsername())) {
            userRepository.findByUsername(request.getUsername())
                    .ifPresent(u -> {
                        throw new DuplicateUsernameException(request.getUsername());
                    });
        }

        if (request.getFirstName() != null) {
            existingUser.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            existingUser.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            existingUser.setEmail(request.getEmail());
        }
        if (request.getUsername() != null) {
            existingUser.setUsername(request.getUsername());
        }
        if (request.getPhone() != null) {
            existingUser.setPhone(request.getPhone());
        }
        if (request.getStatus() != null) {
            existingUser.setStatus(request.getStatus());
        }

        log.info("User updated: {}", id);
        return userRepository.save(existingUser);
    }

    /**
     * Create a new user entity.
     *
     * <p>Performs duplicate checks and creates a default saving account for the user.
     * Wraps persistence in a transaction and converts low-level exceptions to BusinessException.</p>
     *
     * @param user the User entity to persist
     * @return the persisted User
     * @throws DuplicateEmailException          when email already exists
     * @throws DuplicateUsernameException       when username already exists
     * @throws DuplicateIdentificationException when identification number already exists
     * @throws BusinessException                when persistence fails for other reasons
     */
    @Transactional
    public User createUser(User user) {
        log.info("Creating user with email: {}", user.getEmail());

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            log.warn("Attempt to create user with already registered email: {}", user.getEmail());
            throw new DuplicateEmailException(user.getEmail());
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            log.warn("Attempt to create user with already registered username: {}", user.getUsername());
            throw new DuplicateUsernameException(user.getUsername());
        }

        if (userRepository.findByIdentificationNumber(user.getIdentificationNumber()).isPresent()) {
            log.warn("Attempt to create user with already registered identification: {}",
                    user.getIdentificationNumber());
            throw new DuplicateIdentificationException(user.getIdentificationNumber());
        }

        try {
            SavingAccount account = new SavingAccount(
                    user,
                    BigDecimal.ZERO,
                    SavingAccountStatus.ACTIVE,
                    BigDecimal.ZERO
            );
            account.setUser(user);
            user.getAccounts().add(account);

            User savedUser = userRepository.save(user);
            log.info("User created successfully with ID: {}", savedUser.getId());
            log.info("Associated accounts: {}", savedUser.getAccounts().size());

            return savedUser;

        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw new BusinessException("Error creating user", e);
        }
    }

    /**
     * Update an existing user entity.
     *
     * <p>Performs duplicate checks for email and username and persists updates.</p>
     *
     * @param id          UUID of the user to update
     * @param updatedUser the User entity containing updated values
     * @return the updated User
     * @throws UserNotFoundException            when the user does not exist
     * @throws DuplicateEmailException          when the new email is already in use
     * @throws DuplicateUsernameException       when the new username is already in use
     */
    @Transactional
    public User updateUser(UUID id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            userRepository.findByEmail(updatedUser.getEmail())
                    .ifPresent(u -> {
                        throw new DuplicateEmailException(updatedUser.getEmail());
                    });
        }

        if (!existingUser.getUsername().equals(updatedUser.getUsername())) {
            userRepository.findByUsername(updatedUser.getUsername())
                    .ifPresent(u -> {
                        throw new DuplicateUsernameException(updatedUser.getUsername());
                    });
        }

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setStatus(updatedUser.getStatus());

        log.info("User updated: {}", id);
        return userRepository.save(existingUser);
    }

    /**
     * Delete a user by id.
     *
     * @param id UUID of the user to delete
     * @throws UserNotFoundException when the user does not exist
     */
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        log.info("Deleting user: {}", id);
        userRepository.delete(user);
    }

    /**
     * Retrieve top saving users (paginated).
     *
     * @param pageable paging information
     * @return list of TopUserDTO representing top savers
     */
    public List<TopUserDTO> getTopSavers(Pageable pageable) {
        return userRepository.getTopSavers(pageable);
    }

    /**
     * Count users by status.
     *
     * @param status user status to count
     * @return number of users with the given status
     */
    public Long countByStatus(UserStatus status) {
        return userRepository.countByStatus(status);
    }

    /**
     * Find a user by username.
     *
     * @param username the username to search for
     * @return Optional containing the User if found
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find a user by email.
     *
     * @param email the email to search for
     * @return Optional containing the User if found
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find a user by identification number.
     *
     * @param identificationNumber identification number to search
     * @return Optional containing the User if found
     */
    public Optional<User> findByIdentificationNumber(String identificationNumber) {
        return userRepository.findByIdentificationNumber(identificationNumber);
    }
}
