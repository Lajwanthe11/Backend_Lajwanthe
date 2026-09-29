package com.example.platformadmin.superadmin.platformconfiguration.controller;

import com.example.platformadmin.superadmin.platformconfiguration.dto.request.PlatformConfigurationCreateRequest;
import com.example.platformadmin.superadmin.platformconfiguration.dto.request.PlatformConfigurationStatusUpdateRequest;
import com.example.platformadmin.superadmin.platformconfiguration.dto.request.PlatformConfigurationUpdateRequest;
import com.example.platformadmin.superadmin.platformconfiguration.dto.response.PlatformConfigurationHistoryResponse;
import com.example.platformadmin.superadmin.platformconfiguration.dto.response.PlatformConfigurationResponse;
import com.example.platformadmin.superadmin.platformconfiguration.service.PlatformConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Platform Configuration management APIs (FR-001.2).
 */
@RestController
@RequestMapping("/v1/platform-configurations")
@SecurityRequirement(name = "bearerAuth")  
@Tag(name = "Platform Configuration", description = "Platform configuration management APIs (FRS: FR-001.2)")
public class PlatformConfigurationController {

    private static final Logger log = LoggerFactory.getLogger(PlatformConfigurationController.class);
    private final PlatformConfigurationService service;

    public PlatformConfigurationController(PlatformConfigurationService service) {
        this.service = service;
    }

    /**
     * Searches and retrieves all platform configurations matching optional filter criteria.
     *
     * @param name        optional partial configuration name filter
     * @param category    optional exact configuration category filter
     * @param environment optional operational environment tier
     * @param status      optional lifecycle status (ACTIVE or INACTIVE)
     * @return 200 OK with the list of matching configuration responses
     */
    @GetMapping
    @Operation(summary = "Retrieve all configurations", description = "Retrieves all configurations matching optional criteria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configurations retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<PlatformConfigurationResponse>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String status) {
        log.info("REST request to search configurations: name='{}', category='{}', env={}, status={}",
                name, category, environment, status);
        return ResponseEntity.ok(service.getAll(name, category, environment, status));
    }

    /**
     * Retrieves a single platform configuration by its unique identifier.
     *
     * @param id unique identifier of the platform configuration
     * @return 200 OK with the configuration details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Retrieve configuration by ID", description = "Retrieves configuration by unique UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Configuration not found (ERR-0011)")
    })
    public ResponseEntity<PlatformConfigurationResponse> getById(@PathVariable UUID id) {
        log.debug("REST request to fetch configuration by id: {}", id);
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * Creates and persists a new platform configuration.
     *
     * @param request valid payload containing initial configuration settings
     * @return 201 CREATED with the persisted configuration details
     */
    @PostMapping
    @Operation(summary = "Create configuration", description = "Creates a new configuration with pre-activation and master data validation.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Configuration created successfully (NTF-0009)"),
            @ApiResponse(responseCode = "400", description = "Validation failure or pre-activation error (ERR-0008, ERR-0009)"),
            @ApiResponse(responseCode = "409", description = "Duplicate configuration name (ERR-0007)")
    })
    public ResponseEntity<PlatformConfigurationResponse> create(@Valid @RequestBody PlatformConfigurationCreateRequest request) {
        log.info("REST request to create configuration: name='{}'", request.configurationName());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    /**
     * Updates an existing platform configuration by unique identifier.
     *
     * @param id      unique identifier of the configuration to update
     * @param request valid payload containing modified settings
     * @return 200 OK with the updated configuration details
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update configuration", description = "Updates configuration settings.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuration updated successfully (NTF-0010)"),
            @ApiResponse(responseCode = "400", description = "Invalid configuration data (ERR-0008)"),
            @ApiResponse(responseCode = "404", description = "Configuration not found (ERR-0011)"),
            @ApiResponse(responseCode = "409", description = "Duplicate configuration name (ERR-0007)")
    })
    public ResponseEntity<PlatformConfigurationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody PlatformConfigurationUpdateRequest request) {
        log.info("REST request to update configuration id: {}", id);
        return ResponseEntity.ok(service.update(id, request));
    }

    /**
     * Updates the operational status (ACTIVE/INACTIVE) of a platform configuration.
     *
     * @param id      unique identifier of the configuration
     * @param request payload containing target status
     * @return 200 OK with updated configuration details
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update status", description = "Activates or deactivates configuration with pre-activation validation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully (NTF-0011)"),
            @ApiResponse(responseCode = "400", description = "Pre-activation failure or invalid status (ERR-0008, ERR-0009)"),
            @ApiResponse(responseCode = "404", description = "Configuration not found (ERR-0011)")
    })
    public ResponseEntity<PlatformConfigurationResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody PlatformConfigurationStatusUpdateRequest request) {
        log.info("REST request to update status for id: {} to {}", id, request.status());
        return ResponseEntity.ok(service.updateStatus(id, request));
    }

    /**
     * Soft-deletes a platform configuration while preserving its historical audit snapshots.
     *
     * @param id unique identifier of the configuration to soft delete
     * @return 204 NO CONTENT
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete configuration", description = "Soft deletes configuration while preserving history (BR-11).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Configuration soft deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Configuration not found (ERR-0011)")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("REST request to delete configuration id: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restores an existing platform configuration to standard enterprise default settings.
     *
     * @param id unique identifier of the configuration to reset
     * @return 200 OK with default configuration values restored
     */
    @PostMapping("/{id}/restore-default")
    @Operation(summary = "Restore defaults", description = "Restores configuration settings to enterprise defaults.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuration restored to defaults (NTF-0014)"),
            @ApiResponse(responseCode = "404", description = "Configuration not found (ERR-0011)")
    })
    public ResponseEntity<PlatformConfigurationResponse> restoreDefaultConfiguration(@PathVariable UUID id) {
        log.info("REST request to restore defaults for configuration id: {}", id);
        return ResponseEntity.ok(service.restoreDefaultConfiguration(id));
    }

    /**
     * Retrieves the audit revision history snapshots for a given configuration.
     *
     * @param id unique identifier of the configuration
     * @return 200 OK with the descending list of revision snapshots
     */
    @GetMapping("/{id}/history")
    @Operation(summary = "Retrieve history snapshots", description = "Retrieves revision history snapshots (BR-0016).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuration history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Configuration not found (ERR-0011)")
    })
    public ResponseEntity<List<PlatformConfigurationHistoryResponse>> getHistory(@PathVariable UUID id) {
        log.debug("REST request to retrieve revision history for configuration id: {}", id);
        return ResponseEntity.ok(service.getHistory(id));
    }

    /**
     * Rolls back a platform configuration to a previously recorded snapshot version.
     *
     * @param id      unique identifier of the configuration
     * @param version snapshot version number to restore
     * @return 200 OK with the restored configuration state
     */
    @PostMapping("/{id}/rollback/{version}")
    @Operation(summary = "Rollback version", description = "Rolls back configuration to a historical snapshot (BR-0016).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuration rolled back successfully (NTF-0016)"),
            @ApiResponse(responseCode = "404", description = "Configuration or snapshot not found (ERR-0011)")
    })
    public ResponseEntity<PlatformConfigurationResponse> rollback(@PathVariable UUID id, @PathVariable int version) {
        log.info("REST request to rollback configuration id: {} to version {}", id, version);
        return ResponseEntity.ok(service.rollbackToVersion(id, version));
    }
    
}