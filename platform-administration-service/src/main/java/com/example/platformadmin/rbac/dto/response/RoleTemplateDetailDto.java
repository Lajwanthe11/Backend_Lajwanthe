package com.example.platformadmin.rbac.dto.response;


import java.util.Set;
import java.util.UUID;

public class RoleTemplateDetailDto {

    private UUID id;
    private String name;
    private String description;
    private String recommendedFor;
    private Set<String> permissionCodes;

    public RoleTemplateDetailDto(UUID id, String name, String description,
                                 String recommendedFor, Set<String> permissionCodes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.recommendedFor = recommendedFor;
        this.permissionCodes = permissionCodes;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getRecommendedFor() { return recommendedFor; }
    public Set<String> getPermissionCodes() { return permissionCodes; }
}