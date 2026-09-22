package com.example.platformadmin.superadmin.platform_settings_service.repository;

import com.example.platformadmin.superadmin.platform_settings_service.entity.PlatformSettingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlatformSettingHistoryRepository extends JpaRepository<PlatformSettingHistory, UUID> {

    Optional<PlatformSettingHistory> findTopBySettingIdOrderByVersionNumberDesc(UUID settingId);

    List<PlatformSettingHistory> findBySettingIdOrderByVersionNumberDesc(UUID settingId);

}