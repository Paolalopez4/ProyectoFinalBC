package org.pasantia.ahorraya.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pasantia.ahorraya.model.MicroSavingConfig;
import org.pasantia.ahorraya.service.MicroSavingConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller that exposes endpoints to manage MicroSavingConfig entities.
 *
 * <p>This controller provides operations to activate, deactivate and retrieve
 * the active micro-saving configuration for a given user. Endpoints require
 * USER or ADMIN roles as indicated by method-level security annotations.</p>
 */
@RestController
@RequestMapping("/api/micro-saving-configs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Micro Saving Config", description = "Micro saving config management endpoints")
public class MicroSavingConfigController {

    /**
     * Service that encapsulates business logic related to micro-saving configurations.
     */
    private final MicroSavingConfigService service;

    /**
     * Activate a micro-saving configuration by its identifier.
     *
     * @param configId the UUID of the MicroSavingConfig to activate
     * @return HTTP 200 with the activated MicroSavingConfig
     */
    @Operation(summary = "Activate Micro Saving Config", description = "Activates a micro saving configuration by its ID.")
    @PostMapping("/{configId}/activate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MicroSavingConfig> activate(@PathVariable UUID configId) {
        log.info("Activating micro-saving configuration: {}", configId);
        MicroSavingConfig config = service.activate(configId);
        return ResponseEntity.ok(config);
    }

    /**
     * Deactivate a micro-saving configuration by its identifier.
     *
     * @param configId the UUID of the MicroSavingConfig to deactivate
     * @return HTTP 200 with the deactivated MicroSavingConfig
     */
    @Operation(summary = "Deactivate Micro Saving Config", description = "Deactivates a micro saving configuration by its ID.")
    @PostMapping("/{configId}/deactivate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MicroSavingConfig> deactivate(@PathVariable UUID configId) {
        log.info("Deactivating micro-saving configuration: {}", configId);
        MicroSavingConfig config = service.deactivate(configId);
        return ResponseEntity.ok(config);
    }

    /**
     * Retrieve the active micro-saving configuration for a specific user.
     *
     * @param userId the UUID of the user whose active configuration is requested
     * @return HTTP 200 with the MicroSavingConfig if present, or HTTP 204 if none exists
     */
    @Operation(summary = "Get Active Micro Saving Config by User", description = "Retrieves the active micro saving configuration for a specific user.")
    @GetMapping("/user/{userId}/active")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MicroSavingConfig> getActive(@PathVariable UUID userId) {
        log.info("Retrieving active micro-saving configuration for user: {}", userId);
        return service.getActiveConfigByUser(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}