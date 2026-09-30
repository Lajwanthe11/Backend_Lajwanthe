package com.example.auth.devicemanagement.device.service;

import com.example.auth.devicemanagement.device.dto.DeviceActionRequest;
import com.example.auth.devicemanagement.device.dto.DeviceAuditLogResponse;
import com.example.auth.devicemanagement.device.dto.DeviceRegistrationRequest;
import com.example.auth.devicemanagement.device.dto.DeviceResponse;
import com.example.auth.devicemanagement.device.dto.DeviceSummaryResponse;
import com.example.auth.devicemanagement.device.entity.Device;
import com.example.auth.devicemanagement.device.entity.DeviceStatus;
import com.example.auth.devicemanagement.device.entity.DeviceType;
import com.example.common.abstracts.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service contract for Device management.
 * Standard CRUD (create, getById, getAll, update, deleteById, existsById) is inherited from
 * {@link BaseService}; the methods below are device-specific.
 */
public interface DeviceService
        extends BaseService<Device, Long, DeviceRegistrationRequest, DeviceResponse> {

    // getById(Long) is inherited from BaseService

    DeviceResponse registerDevice(DeviceRegistrationRequest request, String username, String userAgent);

    /**
     * Called directly from the login flow (same process now, no network hop) to check
     * whether the device attempting to log in is blocked. Returns true if login should
     * be denied.
     */
    boolean isDeviceBlocked(String deviceIdentifier);

    Page<DeviceResponse> search(String keyword, DeviceType deviceType, DeviceStatus deviceStatus, Pageable pageable);

    DeviceSummaryResponse getSummary();

    DeviceResponse trustDevice(Long id);

    DeviceResponse untrustDevice(Long id);

    DeviceResponse blockDevice(Long id, DeviceActionRequest request);

    DeviceResponse unblockDevice(Long id);

    void removeDevice(Long id, DeviceActionRequest request);

    List<DeviceAuditLogResponse> getAuditLogs(Long deviceId);
}