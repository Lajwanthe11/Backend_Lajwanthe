package com.example.platformadmin.organizations.company.entity;

import com.example.common.abstracts.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing a Company in the platform.
 * Extends {@link BaseEntity} which provides id, audit fields (createdAt, updatedAt, etc.)
 * and multi-tenant isolation via the tenantId column.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

    @Column(name = "company_code", nullable = false, unique = true)
    private String companyCode;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "email")
    private String email;

    @Column(name = "industry")
    private String industry;

    @Column(name = "country")
    private String country;

    @Column(name = "status")
    private String status;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;
}
