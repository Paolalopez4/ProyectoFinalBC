package org.pasantia.ahorraya.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Embeddable audit information for entities.
 * Stores creation and last update timestamps for tracking entity changes.
 */
@Getter
@Embeddable
public class Audit {
    /**
     * Timestamp when the entity was created.
     */
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    /**
     * Timestamp when the entity was last updated.
     */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Initializes creation and update timestamps before persisting the entity.
     */
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Updates the last update timestamp to the current instant.
     */
    public void updateTimestamp() {
        this.updatedAt = Instant.now();
    }
}
