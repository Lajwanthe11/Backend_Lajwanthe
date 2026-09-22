package com.example.platformadmin.superadmin.platform_settings_service.repository;

import com.example.platformadmin.superadmin.platform_settings_service.entity.PlatformSetting;
import com.example.platformadmin.superadmin.platform_settings_service.enums.SettingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlatformSettingRepository extends JpaRepository<PlatformSetting, UUID> {

    Optional<PlatformSetting> findBySettingName(String settingName);

    boolean existsBySettingName(String settingName);

    // Supports the FRS Search + Filter controls without creating separate APIs.
    // Any null/blank filter is ignored.
    @Query("""
            SELECT p
            FROM PlatformSetting p
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(p.settingName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:category IS NULL OR :category = '' OR LOWER(p.category) = LOWER(:category))
              AND (:status IS NULL OR p.status = :status)
            ORDER BY p.settingName
            """)
    List<PlatformSetting> searchAndFilter(@Param("search") String search,
                                          @Param("category") String category,
                                          @Param("status") SettingStatus status);

}