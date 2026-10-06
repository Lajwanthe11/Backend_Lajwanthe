package com.example.auth.devicemanagement.device.controller;

import com.example.auth.devicemanagement.device.dto.DeviceActionRequest;
import com.example.auth.devicemanagement.device.dto.DeviceAuditLogResponse;
import com.example.auth.devicemanagement.device.dto.DeviceRegistrationRequest;
import com.example.auth.devicemanagement.device.dto.DeviceResponse;
import com.example.auth.devicemanagement.device.dto.DeviceSummaryResponse;
import com.example.auth.devicemanagement.device.entity.Device;
import com.example.auth.devicemanagement.device.entity.DeviceStatus;
import com.example.auth.devicemanagement.device.entity.DeviceType;
import com.example.auth.devicemanagement.device.service.DeviceService;
import com.example.common.abstracts.AbstractController;
import com.example.common.response.ApiResponse;
import com.example.common.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Device management.
 * <p>
 * Inherits standard CRUD (create, getById, getAll, update, delete) from {@link AbstractController}
 * and adds device-specific actions: register, search, summary, trust/untrust, block/unblock,
 * remove-with-reason and audit logs.
 * <p>
 * Access: all endpoints are restricted to SUPER_ADMIN / SECURITY_ADMIN via the class-level
 * {@code @PreAuthorize}, except {@code /register}, which any authenticated user may call.
 */
@RestController
@RequestMapping("/devices")
@PreAuthorize(DeviceController.ADMIN_ROLES) // applies to inherited CRUD endpoints too
public class DeviceController
        extends AbstractController<Device, Long, DeviceRegistrationRequest, DeviceResponse> {

    /** SpEL expression for admin-only access. Package-private so the class-level annotation can use it. */
    static final String ADMIN_ROLES = "hasAnyRole('SUPER_ADMIN','SECURITY_ADMIN')";

    /** Typed reference for device-specific operations (the parent only holds the generic BaseService). */
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        // Pass the service to AbstractController so the inherited CRUD endpoints work.
        // DeviceService must extend BaseService<Device, Long, DeviceRegistrationRequest, DeviceResponse>.
        super(deviceService);
        this.deviceService = deviceService;
    }

    // ---------------------------------------------------------------
    // Inherited from AbstractController (admin-only via class-level @PreAuthorize):
    //   POST   /devices          create
    //   GET    /devices/{id}     getById
    //   GET    /devices          getAll (paged)
    //   GET    /devices/all      getAllUnpaged
    //   PUT    /devices/{id}     update
    //   DELETE /devices/{id}     delete
    // ---------------------------------------------------------------

    /**
     * Registers a device for the currently authenticated user.
     * Overrides the class-level rule: any authenticated user may register their own device.
     * Captures the username from the security context and the User-Agent header for the device record.
     */
    @PostMapping("/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DeviceResponse>> register(
            @Valid @RequestBody DeviceRegistrationRequest request,
            Authentication authentication,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {

        DeviceResponse response = deviceService.registerDevice(request, authentication.getName(), userAgent);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Device registered successfully", response));
    }

    /**
     * Paged device search. All filters are optional:
     * keyword (free text), deviceType and deviceStatus. Defaults to 20 per page, newest first.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<DeviceResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DeviceType deviceType,
            @RequestParam(required = false) DeviceStatus deviceStatus,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<DeviceResponse> page = deviceService.search(keyword, deviceType, deviceStatus, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    /** Returns aggregate device statistics (e.g. counts by status/type) for the admin dashboard. */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DeviceSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok(deviceService.getSummary()));
    }

    /** Marks a device as trusted. */
    @PostMapping("/{id}/trust")
    public ResponseEntity<ApiResponse<DeviceResponse>> trustDevice(@PathVariable Long id) {
        validateId(id); // inherited helper: rejects null / non-positive IDs
        return ResponseEntity.ok(ApiResponse.ok("Device marked as trusted", deviceService.trustDevice(id)));
    }

    /** Removes the trusted status from a device. */
    @PostMapping("/{id}/untrust")
    public ResponseEntity<ApiResponse<DeviceResponse>> untrustDevice(@PathVariable Long id) {
        validateId(id);
        return ResponseEntity.ok(ApiResponse.ok("Trust removed from device", deviceService.untrustDevice(id)));
    }

    /** Blocks a device. The request body (e.g. a reason) is optional and recorded for audit. */
    @PostMapping("/{id}/block")
    public ResponseEntity<ApiResponse<DeviceResponse>> blockDevice(
            @PathVariable Long id,
            @RequestBody(required = false) DeviceActionRequest request) {
        validateId(id);
        return ResponseEntity.ok(ApiResponse.ok("Device blocked", deviceService.blockDevice(id, request)));
    }

    /** Reactivates a previously blocked device. */
    @PostMapping("/{id}/unblock")
    public ResponseEntity<ApiResponse<DeviceResponse>> unblockDevice(@PathVariable Long id) {
        validateId(id);
        return ResponseEntity.ok(ApiResponse.ok("Device reactivated", deviceService.unblockDevice(id)));
    }

    /**
     * Removes a device, with an optional reason for the audit trail.
     * Uses {@code /{id}/remove} to avoid clashing with the inherited {@code DELETE /{id}}.
     */
    @DeleteMapping("/{id}/remove")
    public ResponseEntity<ApiResponse<Void>> removeDevice(
            @PathVariable Long id,
            @RequestBody(required = false) DeviceActionRequest request) {
        validateId(id);
        deviceService.removeDevice(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Device removed successfully", null));
    }

    /** Returns the audit log entries (trust, block, remove, etc.) for a device. */
    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<ApiResponse<List<DeviceAuditLogResponse>>> getAuditLogs(@PathVariable Long id) {
        validateId(id);
        return ResponseEntity.ok(ApiResponse.ok(deviceService.getAuditLogs(id)));
    }
}