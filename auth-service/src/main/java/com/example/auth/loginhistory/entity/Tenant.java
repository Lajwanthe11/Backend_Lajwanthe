package com.example.auth.tenant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "tenants")
public class Tenant implements Serializable {

    @Id
    @Column(name = "tenant_id", nullable = false, length = 255)
    private String tenantId;

    @Column(name = "name")
    private String name;

    protected Tenant() {}

    public Tenant(String tenantId, String name) {
        this.tenantId = tenantId;
        this.name = name;
    }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}