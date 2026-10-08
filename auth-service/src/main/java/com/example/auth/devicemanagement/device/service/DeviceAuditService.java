package com.example.auth.devicemanagement.device.service;

import com.example.auth.devicemanagement.config.DeviceManagementProperties;
import com.example.auth.devicemanagement.device.dto.DeviceAuditLogResponse;
import com.example.auth.devicemanagement.device.entity.Device;
import com.example.auth.devicemanagement.device.entity.DeviceAuditAction;
import com.example.auth.devicemanagement.device.entity.DeviceAuditLog;
import com.example.auth.devicemanagement.device.repository.DeviceAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles writing and reading device audit logs.
 * Keeps audit concerns out of {@link DeviceServiceImpl}.
 */
@Service
public class DeviceAuditService {

    private final DeviceAuditLogRepository auditLogRepository;
    private final DeviceManagementProperties properties;

    public DeviceAuditService(DeviceAuditLogRepository auditLogRepository,
                              DeviceManagementProperties properties) {
        this.auditLogRepository = auditLogRepository;
        this.properties = properties;
    }

    /**
     * Records an action performed on a device.
     * Does nothing when audit logging is disabled in configuration.
     */
    @Transactional
    public void log(Device device, DeviceAuditAction action, String reason) {
        if (!properties.isAuditLoggingEnabled()) {
            return;
        }
        DeviceAuditLog entry = new DeviceAuditLog();
        entry.setDeviceId(device.getId());
        entry.setDeviceIdentifier(device.getDeviceIdentifier());
        entry.setAction(action);
        entry.setReason(reason);
        auditLogRepository.save(entry);
    }

    /** Returns all audit entries for a device, newest first. */
    @Transactional(readOnly = true)
    public List<DeviceAuditLogResponse> getLogs(Long deviceId) {
        return auditLogRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId).stream()
                .map(this::toResponse)
                .toList();
    }

    private DeviceAuditLogResponse toResponse(DeviceAuditLog entry) {
        DeviceAuditLogResponse dto = new DeviceAuditLogResponse();
        dto.setId(entry.getId());
        dto.setDeviceId(entry.getDeviceId());
        dto.setDeviceIdentifier(entry.getDeviceIdentifier());
        dto.setAction(entry.getAction());
        dto.setReason(entry.getReason());
        dto.setPerformedBy(entry.getCreatedBy());
        dto.setPerformedAt(entry.getCreatedAt());
        return dto;
    }
}