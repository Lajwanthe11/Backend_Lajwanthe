package com.example.rbac.entity;

import com.example.common.abstracts.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "role_templates")
public class RoleTemplate extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "recommended_for", length = 150)
    private String recommendedFor;

    // Templates cannot be deleted but can be hidden by super admin
    @Column(nullable = false)
    private boolean hidden = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_template_permissions", joinColumns = @JoinColumn(name = "template_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private List<Permission> permissions = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        setUpdatedAt(LocalDateTime.from(Instant.now())) ;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecommendedFor() {
        return recommendedFor;
    }

    public void setRecommendedFor(String recommendedFor) {
        this.recommendedFor = recommendedFor;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public List<Permission> getPermissions() { return permissions; }
    public void setPermissions(List<Permission> permissions) { this.permissions = permissions; }

}