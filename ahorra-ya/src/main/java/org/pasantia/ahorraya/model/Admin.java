package org.pasantia.ahorraya.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.pasantia.ahorraya.model.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Represents an administrator account used to manage the application.
 *
 * <p>This JPA entity stores authentication and authorization information for
 * administrators. It implements {@link UserDetails} so it can be used directly
 * by Spring Security. Audit information is embedded via {@link Audit}.</p>
 */
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "admins")
public class Admin implements UserDetails {

    /**
     * Unique identifier for the admin (UUID).
     *
     * <p>Generated and immutable after creation.</p>
     */
    @Getter
    @NotNull
    @UuidGenerator
    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * Administrator email address used as the login username.
     *
     * <p>Stored in lowercase. Must be a valid email address and unique.</p>
     */
    @Getter
    @Setter
    @NotNull
    @Email(message = "Email must be a valid email address")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * Hashed password for the administrator.
     *
     * <p>Must not be null. Stored as an encoded string.</p>
     */
    @Setter
    @NotNull
    @Column(nullable = false)
    private String password;

    /**
     * Role assigned to this admin (defaults to ADMIN).
     */
    @Getter
    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ADMIN;

    /**
     * Indicates whether the admin account is active.
     *
     * <p>Used to enable/disable authentication and account access.</p>
     */
    @Getter
    @Setter
    @NotNull
    @Column(nullable = false)
    private boolean active;

    /**
     * Embedded audit information (creation/update timestamps, etc.).
     */
    @Getter
    @NotNull
    @Builder.Default
    @Embedded
    private Audit audit = new Audit();

    /**
     * Normalize fields and populate audit data before persisting.
     *
     * <p>Converts the email to lowercase and calls audit pre-persist logic.</p>
     */
    @PrePersist
    private void prePersist() {
        this.email = this.email != null ? this.email.toLowerCase() : null;
        this.audit.prePersist();
    }

    /**
     * Normalize fields and update audit timestamp before updating.
     *
     * <p>Converts the email to lowercase and updates the audit timestamp.</p>
     */
    @PreUpdate
    private void preUpdate() {
        this.email = this.email != null ? this.email.toLowerCase() : null;
        this.audit.updateTimestamp();
    }

    /**
     * Return granted authorities for this admin.
     *
     * @return a collection containing the ROLE_ADMIN authority
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
    }

    /**
     * Return the encoded password.
     *
     * @return password hash
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Return the username used to authenticate (email).
     *
     * @return admin email
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indicates whether the account has expired. Always true for admins.
     *
     * @return true if account is non-expired
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the account is non-locked.
     *
     * @return true if the account is active (non-locked)
     */
    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    /**
     * Indicates whether the credentials are non-expired. Always true.
     *
     * @return true if credentials are non-expired
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the account is enabled.
     *
     * @return true if the account is active
     */
    @Override
    public boolean isEnabled() {
        return active;
    }
}
