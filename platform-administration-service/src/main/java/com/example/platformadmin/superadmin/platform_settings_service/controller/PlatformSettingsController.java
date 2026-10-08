package com.example.platformadmin.superadmin.platform_settings_service.controller;

import com.example.platformadmin.superadmin.platform_settings_service.dto.request.CreatePlatformSettingsRequest;
import com.example.platformadmin.superadmin.platform_settings_service.dto.request.UpdatePlatformSettingsRequest;
import com.example.platformadmin.superadmin.platform_settings_service.dto.request.UpdateSettingStatusRequest;
import com.example.platformadmin.superadmin.platform_settings_service.dto.response.ErrorResponse;
import com.example.platformadmin.superadmin.platform_settings_service.dto.response.PlatformSettingsHistoryResponse;
import com.example.platformadmin.superadmin.platform_settings_service.dto.response.PlatformSettingsResponse;
import com.example.platformadmin.superadmin.platform_settings_service.enums.SettingStatus;
import com.example.platformadmin.superadmin.platform_settings_service.service.PlatformSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/platform-settings")
@RequiredArgsConstructor
@Tag(name = "Platform Settings Controller", description = "APIs for managing global platform-wide settings")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformSettingsController {

    private final PlatformSettingsService service;

    // ---------------------------------------------------------
    // CREATE PLATFORM SETTING
    // ---------------------------------------------------------

    @PostMapping
    @Operation(summary = "Create platform setting",
            description = "Creates a new global platform setting after server-side validation. " +
                    "The setting is stored as VALIDATED and becomes effective after activation.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Platform setting created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid platform setting values",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User is not authorized to create platform settings",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PlatformSettingsResponse> createSetting(
            @Valid @RequestBody CreatePlatformSettingsRequest request) {

        log.info("Received request to create platform setting. settingName={}", request.getSettingName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createSetting(request));
    }

    // ---------------------------------------------------------
    // GET ALL SETTINGS WITH OPTIONAL FILTERS
    // ---------------------------------------------------------

    @GetMapping
    @Operation(summary = "Get all platform settings",
            description = "Retrieves the current platform-wide global settings.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform settings retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlatformSettingsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<PlatformSettingsResponse>> getAllSettings(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) SettingStatus status) {

        log.info("Received request to fetch platform settings. search={}, category={}, status={}",
                search, category, status);

        return ResponseEntity.ok(service.getAllSettings(search, category, status));
    }

    // ---------------------------------------------------------
    // GET SETTING BY KEY
    // ---------------------------------------------------------

    @GetMapping("/{key}")
    @Operation(summary = "Get platform settings by key",
            description = "Retrieves a specific platform settings configuration using its unique setting key. " +
                    "For the global platform configuration, use GLOBAL_SETTINGS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform setting retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Platform setting not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PlatformSettingsResponse> getSetting(
            @Parameter(description = "Unique platform setting key", example = "GLOBAL_SETTINGS", required = true)
            @PathVariable String key) {

        log.info("Received request to fetch platform setting. key={}", key);

        return ResponseEntity.ok(service.getSetting(key));
    }

    // ---------------------------------------------------------
    // UPDATE PLATFORM SETTINGS
    // ---------------------------------------------------------

    @PutMapping("/{key}")
    @Operation(summary = "Update platform settings",
            description = "Updates Global Settings after server-side validation. " +
                    "The validated configuration is versioned and audited. It becomes effective after activation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform settings updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid platform setting values",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User is not authorized to modify platform settings",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Platform setting not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PlatformSettingsResponse> updateSetting(
            @Parameter(description = "Unique platform setting key", example = "GLOBAL_SETTINGS", required = true)
            @PathVariable String key,
            @Valid @RequestBody UpdatePlatformSettingsRequest request) {

        log.info("Received request to update platform setting. key={}", key);

        return ResponseEntity.ok(service.updateSetting(key, request));
    }

    // ---------------------------------------------------------
    // UPDATE STATUS
    // ---------------------------------------------------------

    @PatchMapping("/{key}/status")
    @Operation(summary = "Update platform setting status",
            description = "Activates or deactivates Global Settings. Activation validates " +
                    "the current configuration, propagates it, and records an audit event.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform setting status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User is not authorized to update platform setting status",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Platform setting not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PlatformSettingsResponse> updateStatus(
            @Parameter(description = "Unique platform setting key", example = "GLOBAL_SETTINGS", required = true)
            @PathVariable String key,
            @Valid @RequestBody UpdateSettingStatusRequest request) {

        log.info("Received request to update platform setting status. key={}, status={}",
                key, request.getStatus());

        return ResponseEntity.ok(service.updateStatus(key, request));
    }

    // ---------------------------------------------------------
    // RESET SETTINGS
    // ---------------------------------------------------------

    @PostMapping("/reset")
    @Operation(summary = "Restore platform settings to defaults",
            description = "Restores Global Settings to the approved defaults, validates and activates " +
                    "the restored configuration, propagates it, and records an audit event.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Platform settings restored to default values successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User is not authorized to restore platform settings",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Global platform settings configuration not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unable to restore platform settings",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PlatformSettingsResponse> resetSettings() {

        log.info("Received request to restore platform settings to default values");

        return ResponseEntity.ok(service.resetSettings());
    }

    // ---------------------------------------------------------
    // GET SETTING HISTORY
    // ---------------------------------------------------------

    @GetMapping("/{key}/history")
    @Operation(summary = "Get platform setting history",
            description = "Retrieves version history snapshots for a specific platform setting, newest version first.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform setting history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User is not authorized to view platform setting history",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Platform setting not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<PlatformSettingsHistoryResponse>> getSettingHistory(
            @Parameter(description = "Unique platform setting key", example = "GLOBAL_SETTINGS", required = true)
            @PathVariable String key) {

        log.info("Received request to fetch platform setting history. key={}", key);

        return ResponseEntity.ok(service.getSettingHistory(key));
    }

    // ---------------------------------------------------------
    // EXPORT PLATFORM SETTINGS
    // ---------------------------------------------------------

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(summary = "Export platform settings",
            description = "Exports the platform settings matching the optional Search and Filter criteria as CSV.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform settings exported successfully",
                    content = @Content(mediaType = "text/csv")),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User is not authorized to export platform settings",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<byte[]> exportSettings(@RequestParam(required = false) String search,
                                                 @RequestParam(required = false) String category,
                                                 @RequestParam(required = false) SettingStatus status) {

        log.info("Received request to export platform settings. search={}, category={}, status={}",
                search, category, status);

        byte[] csv = service.exportSettings(search, category, status);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=platform-settings.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

}
