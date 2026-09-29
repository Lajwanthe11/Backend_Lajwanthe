package com.example.auth.devicemanagement.device.service;

import com.example.auth.devicemanagement.config.DeviceManagementProperties;
import com.example.auth.devicemanagement.device.dto.DeviceActionRequest;
import com.example.auth.devicemanagement.device.dto.DeviceAuditLogResponse;
import com.example.auth.devicemanagement.device.dto.DeviceRegistrationRequest;
import com.example.auth.devicemanagement.device.dto.DeviceResponse;
import com.example.auth.devicemanagement.device.dto.DeviceSummaryResponse;
import com.example.auth.devicemanagement.device.entity.Device;
import com.example.auth.devicemanagement.device.entity.DeviceAuditAction;
import com.example.auth.devicemanagement.device.entity.DeviceAuditLog;
import com.example.auth.devicemanagement.device.entity.DeviceStatus;
import com.example.auth.devicemanagement.device.entity.DeviceType;
import com.example.auth.devicemanagement.device.entity.TrustStatus;
import com.example.auth.devicemanagement.device.exception.DeviceStateException;
import com.example.auth.devicemanagement.device.repository.DeviceAuditLogRepository;
import com.example.auth.devicemanagement.device.repository.DeviceRepository;
import com.example.auth.devicemanagement.util.UserAgentDetector;
import com.example.common.exception.BadRequestException;
import com.example.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceAuditLogRepository auditLogRepository;
    private final DeviceManagementProperties properties;
    private final UserAgentDetector userAgentDetector;

    public DeviceServiceImpl(DeviceRepository deviceRepository,
                             DeviceAuditLogRepository auditLogRepository,
                             DeviceManagementProperties properties,
                             UserAgentDetector userAgentDetector) {
        this.deviceRepository = deviceRepository;
        this.auditLogRepository = auditLogRepository;
        this.properties = properties;
        this.userAgentDetector = userAgentDetector;
    }

    @Override
    @Transactional
    public DeviceResponse registerDevice(DeviceRegistrationRequest request, String username, String userAgent) {

        if (deviceRepository.existsByDeviceIdentifier(request.getDeviceIdentifier())) {
            Device existing = deviceRepository.findByDeviceIdentifier(request.getDeviceIdentifier())
                    .orElseThrow(() -> new ResourceNotFoundException("Device", "deviceIdentifier", request.getDeviceIdentifier()));
            existing.setLastLoginAt(LocalDateTime.now());
            Device saved = deviceRepository.save(existing);
            writeAuditLog(saved, DeviceAuditAction.LOGIN, null);
            return toResponse(saved);
        }

        int maxDevices = properties.getMaxDevicesPerUser();
        if (maxDevices > 0 && deviceRepository.countByUsername(username) >= maxDevices) {
            throw new BadRequestException(
                    "Maximum number of registered devices (" + maxDevices + ") reached for this account");
        }

        String deviceName = StringUtils.hasText(request.getDeviceName())
                ? request.getDeviceName()
                : userAgentDetector.detectDeviceName(userAgent);

        DeviceType deviceType = request.getDeviceType() != null
                ? request.getDeviceType()
                : userAgentDetector.detectDeviceType(userAgent);

        String operatingSystem = StringUtils.hasText(request.getOperatingSystem())
                ? request.getOperatingSystem()
                : userAgentDetector.detectOperatingSystem(userAgent);

        Device device = new Device();
        device.setDeviceIdentifier(request.getDeviceIdentifier());
        device.setDeviceName(deviceName);
        device.setUsername(username);
        device.setEmployeeId(request.getEmployeeId());
        device.setDeviceType(deviceType);
        device.setOperatingSystem(operatingSystem);
        device.setTrustStatus(properties.getDefaultTrustStatus());
        device.setDeviceStatus(DeviceStatus.ACTIVE);
        device.setLastLoginAt(LocalDateTime.now());

        Device saved = deviceRepository.save(device);
        writeAuditLog(saved, DeviceAuditAction.REGISTERED, null);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDeviceBlocked(String deviceIdentifier) {
        return deviceRepository.findByDeviceIdentifier(deviceIdentifier)
                .map(device -> device.getDeviceStatus() == DeviceStatus.BLOCKED)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getById(Long id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeviceResponse> search(String keyword, DeviceType deviceType, DeviceStatus deviceStatus, Pageable pageable) {
        return deviceRepository.search(keyword, deviceType, deviceStatus, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceSummaryResponse getSummary() {
        long registered = deviceRepository.count();
        long trusted = deviceRepository.countByTrustStatus(TrustStatus.TRUSTED);
        long blocked = deviceRepository.countByDeviceStatus(DeviceStatus.BLOCKED);
        long inactive = deviceRepository.countByDeviceStatus(DeviceStatus.INACTIVE);
        return new DeviceSummaryResponse(registered, trusted, blocked, inactive);
    }

    @Override
    @Transactional
    public DeviceResponse trustDevice(Long id) {
        Device device = findByIdOrThrow(id);
        if (device.getDeviceStatus() != DeviceStatus.ACTIVE) {
            throw new DeviceStateException("Only active devices can be marked as trusted");
        }
        device.setTrustStatus(TrustStatus.TRUSTED);
        Device saved = deviceRepository.save(device);
        writeAuditLog(saved, DeviceAuditAction.TRUSTED, null);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DeviceResponse untrustDevice(Long id) {
        Device device = findByIdOrThrow(id);
        device.setTrustStatus(TrustStatus.UNTRUSTED);
        Device saved = deviceRepository.save(device);
        writeAuditLog(saved, DeviceAuditAction.UNTRUSTED, null);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DeviceResponse blockDevice(Long id, DeviceActionRequest request) {
        Device device = findByIdOrThrow(id);
        if (device.getTrustStatus() == TrustStatus.TRUSTED) {
            throw new DeviceStateException("A trusted device must be untrusted before it can be blocked");
        }
        device.setDeviceStatus(DeviceStatus.BLOCKED);
        Device saved = deviceRepository.save(device);
        String reason = request != null ? request.getReason() : null;
        writeAuditLog(saved, DeviceAuditAction.BLOCKED, reason);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DeviceResponse unblockDevice(Long id) {
        Device device = findByIdOrThrow(id);
        device.setDeviceStatus(DeviceStatus.ACTIVE);
        Device saved = deviceRepository.save(device);
        writeAuditLog(saved, DeviceAuditAction.UNBLOCKED, null);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void removeDevice(Long id, DeviceActionRequest request) {
        Device device = findByIdOrThrow(id);
        String reason = request != null ? request.getReason() : null;
        writeAuditLog(device, DeviceAuditAction.REMOVED, reason);
        deviceRepository.delete(device);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceAuditLogResponse> getAuditLogs(Long deviceId) {
        findByIdOrThrow(deviceId);
        return auditLogRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId).stream()
                .map(this::toAuditResponse)
                .toList();
    }

    private Device findByIdOrThrow(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
    }

    private void writeAuditLog(Device device, DeviceAuditAction action, String reason) {
        if (!properties.isAuditLoggingEnabled()) {
            return;
        }
        DeviceAuditLog log = new DeviceAuditLog();
        log.setDeviceId(device.getId());
        log.setDeviceIdentifier(device.getDeviceIdentifier());
        log.setAction(action);
        log.setReason(reason);
        auditLogRepository.save(log);
    }

    private DeviceResponse toResponse(Device device) {
        DeviceResponse dto = new DeviceResponse();
        dto.setId(device.getId());
        dto.setDeviceIdentifier(device.getDeviceIdentifier());
        dto.setDeviceName(device.getDeviceName());
        dto.setUsername(device.getUsername());
        dto.setEmployeeId(device.getEmployeeId());
        dto.setDeviceType(device.getDeviceType());
        dto.setOperatingSystem(device.getOperatingSystem());
        dto.setTrustStatus(device.getTrustStatus());
        dto.setDeviceStatus(device.getDeviceStatus());
        dto.setLastLoginAt(device.getLastLoginAt());
        dto.setRegisteredAt(device.getCreatedAt());
        dto.setUpdatedAt(device.getUpdatedAt());
        dto.setUpdatedBy(device.getUpdatedBy());
        return dto;
    }

    private DeviceAuditLogResponse toAuditResponse(DeviceAuditLog log) {
        DeviceAuditLogResponse dto = new DeviceAuditLogResponse();
        dto.setId(log.getId());
        dto.setDeviceId(log.getDeviceId());
        dto.setDeviceIdentifier(log.getDeviceIdentifier());
        dto.setAction(log.getAction());
        dto.setReason(log.getReason());
        dto.setPerformedBy(log.getCreatedBy());
        dto.setPerformedAt(log.getCreatedAt());
        return dto;
    }
}