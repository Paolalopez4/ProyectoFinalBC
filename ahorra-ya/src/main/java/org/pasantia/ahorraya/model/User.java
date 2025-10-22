package org.pasantia.ahorraya.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.pasantia.ahorraya.model.enums.Role;
import org.pasantia.ahorraya.model.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * JPA entity representing an application user and implementing Spring Security's UserDetails.
 *
 * <p>This entity stores identity, contact, credential, status and audit information
 * for an application user. It owns relationships to saving accounts, micro-saving
 * configurations and expense transactions. Lifecycle callbacks normalize fields
 * (email and identification number) and populate audit timestamps.</p>
 */
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED, force = true)
@Builder
@AllArgsConstructor
@Table(name = "users")
@Entity
public class User implements UserDetails {
    /**
     * Unique identifier for the user.
     */
    @Getter
    @NotNull
    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Builder.Default
    @Setter
    @Getter
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavingAccount> accounts = new ArrayList<>();

    @Builder.Default
    @Setter
    @Getter
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MicroSavingConfig> microSavingConfigs = new ArrayList<>();

    @Builder.Default
    @Setter
    @Getter
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseTransaction> expenseTransactions = new ArrayList<>();

    /**
     * Government-issued identification number (e.g., DUI, passport).
     * Immutable after creation for audit and compliance purposes.
     */
    @Getter
    @Setter
    @NotNull
    @Pattern(regexp = "^[A-Z0-9]{5,20}$", message = "Invalid identification number format")
    @Column(name = "identification_number", nullable = false, unique = true, updatable = false)
    private String identificationNumber;

    /**
     * User's first name.
     */
    @Getter
    @Setter
    @NotNull
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * User's last name.
     */
    @Getter
    @Setter
    @NotNull
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /**
     * User's email address (unique, normalized to lowercase).
     */
    @Getter
    @Setter
    @NotNull
    @Email(message = "Invalid email format")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * User's phone number (must include country code).
     */
    @Getter
    @Setter
    @NotNull
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Column(nullable = false)
    private String phone;

    @Getter
    @Setter
    @NotNull
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * User's hashed password (BCrypt format).
     */
    @Getter
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Current account status (ACTIVE, INACTIVE, SUSPENDED).
     */
    @Getter
    @Setter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * Audit information (creation and modification timestamps).
     */
    @Getter
    @NotNull
    @Embedded
    private final Audit audit = new Audit();

    /**
     * Version for optimistic locking (prevents concurrent modification conflicts).
     */
    @Version
    @Column(name = "version")
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final Role role = Role.USER;

    /**
     * Constructs a new user with the specified information.
     *
     * @param identificationNumber government-issued ID (required)
     * @param firstName           user's first name (required)
     * @param lastName            user's last name (required)
     * @param email               user's email address (required, will be normalized)
     * @param phone               user's phone number with country code (required)
     * @param password            BCrypt-hashed password (required)
     * @param status          initial account status (required)
     * @throws IllegalArgumentException if password appears to be plain text
     */
    public User(String identificationNumber, String firstName, String lastName,
                String email, String phone, String username, String password, UserStatus status) {
        validateHashedPassword(password);

        this.identificationNumber = identificationNumber != null ? identificationNumber.toUpperCase() : null;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email != null ? email.toLowerCase() : null;
        this.phone = phone;
        this.username = username;
        this.password = password;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.accounts = new ArrayList<>();
        this.expenseTransactions = new ArrayList<>();
        this.microSavingConfigs = new ArrayList<>();
    }

    /**
     * Returns the user's full name (first name + last name).
     *
     * @return the concatenated full name
     */
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    /**
     * Activates the user account.
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Deactivates the user account.
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    /**
     * Suspends the user account.
     */
    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }


    /**
     * Checks if the user account is currently active.
     *
     * @return {@code true} if status is ACTIVE, {@code false} otherwise
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    /**
     * Validates that a password appears to be properly hashed (BCrypt format).
     *
     * @param password the password to validate
     * @throws IllegalArgumentException if password is invalid or appears to be plain text
     */
    private void validateHashedPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (!password.matches("^\\$2[ayb]\\$.{56}$")) {
            throw new IllegalArgumentException(
                    "Password must be BCrypt-hashed"
            );
        }
    }

    /**
     * JPA lifecycle callback to normalize data before persisting.
     */
    @PrePersist
    private void prePersist() {
        this.email = this.email != null ? this.email.toLowerCase() : null;
        this.identificationNumber = this.identificationNumber != null ? this.identificationNumber.toUpperCase() : null;
        this.audit.prePersist();
    }

    /**
     * JPA lifecycle callback to normalize data before updating.
     */
    @PreUpdate
    private void preUpdate() {
        this.email = this.email != null ? this.email.toLowerCase() : null;
        this.audit.updateTimestamp();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
