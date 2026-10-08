package com.example.platformadmin.rbac.dto.response;

import java.util.UUID;

public class RoleTemplateSummaryDto {

    private UUID id;
    private String name;
    private String description;
    private Integer permissionCount;
    private String recommendedFor;

    public RoleTemplateSummaryDto(UUID id, String name, String description,
                                  Integer permissionCount, String recommendedFor) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissionCount = permissionCount;
        this.recommendedFor = recommendedFor;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getPermissionCount() { return permissionCount; }
    public String getRecommendedFor() { return recommendedFor; }
}