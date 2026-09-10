package com.enterprise.superadmin.license_management_service.entity;



import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.enums.LicenseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "licenses")
public class License extends BaseEntity {

    @Column(name = "license_key", nullable = false, unique = true, length = 100)
    private String licenseKey;

    @Column(name = "license_plan", nullable = false, length = 100)
    private String licensePlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", length = 50)
    private LicenseType licenseType;

    @Column(name = "activation_date", nullable = false)
    private LocalDate activationDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LicenseStatus status = LicenseStatus.PENDING;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (getCreatedAt() == null) {
            setCreatedAt(now);
        }

        if (getUpdatedAt() == null) {
            setUpdatedAt(now);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdatedAt(LocalDateTime.now());
    }
}