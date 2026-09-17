package com.example.devicemanagement.device.repository;

import com.example.devicemanagement.device.entity.Device;
import com.example.devicemanagement.device.entity.DeviceStatus;
import com.example.devicemanagement.device.entity.DeviceType;
import com.example.devicemanagement.device.entity.TrustStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceIdentifier(String deviceIdentifier);

    boolean existsByDeviceIdentifier(String deviceIdentifier);

    long countByUsername(String username);

    long countByDeviceStatus(DeviceStatus deviceStatus);

    long countByTrustStatus(TrustStatus trustStatus);

    @Query("""
            SELECT d FROM Device d
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(d.deviceName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(d.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(d.employeeId) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(d.deviceIdentifier) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:deviceType IS NULL OR d.deviceType = :deviceType)
              AND (:deviceStatus IS NULL OR d.deviceStatus = :deviceStatus)
            """)
    Page<Device> search(@Param("keyword") String keyword,
                        @Param("deviceType") DeviceType deviceType,
                        @Param("deviceStatus") DeviceStatus deviceStatus,
                        Pageable pageable);
}