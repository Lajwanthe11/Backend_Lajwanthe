package com.example.devicemanagement.device.repository;

import com.example.devicemanagement.device.entity.DeviceAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceAuditLogRepository extends JpaRepository<DeviceAuditLog, Long> {

    List<DeviceAuditLog> findByDeviceIdOrderByCreatedAtDesc(Long deviceId);
}