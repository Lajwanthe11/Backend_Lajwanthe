package com.example.platformadmin.organizations.company.entity;

import com.example.common.abstracts.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * JPA entity representing a Company in the platform.
 * Extends {@link BaseEntity} which provides id, audit fields (createdAt, updatedAt, etc.)
 * and multi-tenant isolation via the tenantId column.
 */
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

    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
