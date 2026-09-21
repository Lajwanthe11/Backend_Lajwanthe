package com.example.auth.devicemanagement.device.controller;

import com.example.auth.devicemanagement.device.dto.DeviceActionRequest;
import com.example.auth.devicemanagement.device.dto.DeviceAuditLogResponse;
import com.example.auth.devicemanagement.device.dto.DeviceRegistrationRequest;
import com.example.auth.devicemanagement.device.dto.DeviceResponse;
import com.example.auth.devicemanagement.device.dto.DeviceSummaryResponse;
import com.example.auth.devicemanagement.device.entity.DeviceStatus;
import com.example.auth.devicemanagement.device.entity.DeviceType;
import com.example.auth.devicemanagement.device.service.DeviceService;
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

@RestController
@RequestMapping("/devices")
public class DeviceController {

    private static final String ADMIN_ROLES = "hasAnyRole('SUPER_ADMIN','SECURITY_ADMIN')";

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

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

    @GetMapping("/{id}")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<DeviceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(deviceService.getById(id)));
    }

    @GetMapping("/search")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<PageResponse<DeviceResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DeviceType deviceType,
            @RequestParam(required = false) DeviceStatus deviceStatus,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<DeviceResponse> page = deviceService.search(keyword, deviceType, deviceStatus, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/summary")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<DeviceSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok(deviceService.getSummary()));
    }

    @PostMapping("/{id}/trust")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<DeviceResponse>> trustDevice(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Device marked as trusted", deviceService.trustDevice(id)));
    }

    @PostMapping("/{id}/untrust")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<DeviceResponse>> untrustDevice(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Trust removed from device", deviceService.untrustDevice(id)));
    }

    @PostMapping("/{id}/block")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<DeviceResponse>> blockDevice(
            @PathVariable Long id,
            @RequestBody(required = false) DeviceActionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Device blocked", deviceService.blockDevice(id, request)));
    }

    @PostMapping("/{id}/unblock")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<DeviceResponse>> unblockDevice(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Device reactivated", deviceService.unblockDevice(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<Void>> removeDevice(
            @PathVariable Long id,
            @RequestBody(required = false) DeviceActionRequest request) {
        deviceService.removeDevice(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Device removed successfully", null));
    }

    @GetMapping("/{id}/audit-logs")
    @PreAuthorize(ADMIN_ROLES)
    public ResponseEntity<ApiResponse<List<DeviceAuditLogResponse>>> getAuditLogs(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(deviceService.getAuditLogs(id)));
    }
}