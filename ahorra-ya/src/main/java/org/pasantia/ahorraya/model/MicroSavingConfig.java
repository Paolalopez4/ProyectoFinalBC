package org.pasantia.ahorraya.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Entity representing a user's micro-saving configuration.
 * Stores the daily savings limit, activation status, versioning, and audit information
 * for micro-saving rules associated with a user.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "micro_saving_configs")
@Entity
public class MicroSavingConfig {
    /**
     * Unique identifier for this configuration (UUID).
     */
    @Getter
    @NotNull
    @UuidGenerator
    @GeneratedValue
    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * The user to whom this micro-saving configuration belongs.
     */
    @Getter
    @Setter
    @NotNull
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Indicates if this configuration is currently active.
     */
    @Getter
    @Setter
    @Column(nullable = false)
    private boolean active;

    /**
     * Version number for this configuration, incremented on each change.
     */
    @Getter
    @NotNull
    @Column(nullable = false)
    private int version;

    /**
     * Embedded audit information (created/updated timestamps).
     */
    @Getter
    @Embedded
    private final Audit audit = new Audit();

    /**
     * Constructs a new MicroSavingConfig with the specified parameters.
     *
     * @param user the user associated with this configuration
     * @param active whether the configuration is active
     * @param version the initial version number
     */
    public MicroSavingConfig(User user, boolean active, int version) {
        this.user = user;
        this.active = active;
        this.version = version;
    }

    /**
     * Activates this configuration and increments the version.
     */
    public void activateConfig() {
        this.active = true;
        this.version += 1;
    }

    /**
     * Deactivates this configuration and increments the version.
     */
    public void deactivateConfig() {
        this.active = false;
        this.version += 1;
    }

    /**
     * JPA lifecycle callback executed before the entity is persisted.
     * Normalizes the daily savings limit and initializes audit information.
     */
    @PrePersist
    private void prePersist() {
        this.audit.prePersist();
    }

    /**
     * JPA lifecycle callback executed before the entity is updated.
     * Normalizes the daily savings limit and updates audit information.
     */
    @PreUpdate
    private void preUpdate() {
        this.audit.updateTimestamp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}