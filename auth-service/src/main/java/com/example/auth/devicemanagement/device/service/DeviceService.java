package com.example.auth.devicemanagement.device.service;

import com.example.auth.devicemanagement.device.dto.DeviceActionRequest;
import com.example.auth.devicemanagement.device.dto.DeviceAuditLogResponse;
import com.example.auth.devicemanagement.device.dto.DeviceRegistrationRequest;
import com.example.auth.devicemanagement.device.dto.DeviceResponse;
import com.example.auth.devicemanagement.device.dto.DeviceSummaryResponse;
import com.example.auth.devicemanagement.device.entity.DeviceStatus;
import com.example.auth.devicemanagement.device.entity.DeviceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeviceService {

    DeviceResponse registerDevice(DeviceRegistrationRequest request, String username, String userAgent);

    /**
     * Called directly from the login flow (same process now, no network hop) to check
     * whether the device attempting to log in is blocked. Returns true if login should
     * be denied.
     */
    boolean isDeviceBlocked(String deviceIdentifier);

    DeviceResponse getById(Long id);

    Page<DeviceResponse> search(String keyword, DeviceType deviceType, DeviceStatus deviceStatus, Pageable pageable);

    DeviceSummaryResponse getSummary();

    DeviceResponse trustDevice(Long id);

    DeviceResponse untrustDevice(Long id);

    DeviceResponse blockDevice(Long id, DeviceActionRequest request);

    DeviceResponse unblockDevice(Long id);

    void removeDevice(Long id, DeviceActionRequest request);

    List<DeviceAuditLogResponse> getAuditLogs(Long deviceId);
}