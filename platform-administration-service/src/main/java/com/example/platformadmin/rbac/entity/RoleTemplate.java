package com.example.platformadmin.rbac.entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "role_templates")
public class RoleTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "recommended_for", length = 150)
    private String recommendedFor;

    @Column(nullable = false)
    private boolean hidden = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_template_permissions",
            joinColumns = @JoinColumn(name = "template_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRecommendedFor() { return recommendedFor; }
    public void setRecommendedFor(String recommendedFor) { this.recommendedFor = recommendedFor; }

    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }

    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }
}
