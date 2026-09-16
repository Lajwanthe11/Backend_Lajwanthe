package com.example.platformadmin.organizations.branches.entity;

import com.example.common.abstracts.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "branches")
public class BranchEntity extends BaseEntity {

    @Column(name = "branch_code", nullable = false, unique = true, length = 50)
    private String branchCode;

    @Column(name = "branch_name", nullable = false, length = 100)
    private String branchName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}