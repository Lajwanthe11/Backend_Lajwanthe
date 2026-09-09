package com.example.rbac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionMatrixResponse {

    private List<RoleColumn> roles;

    private List<PermissionGroupRow> permissionGroups;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleColumn {

        private Long roleId;

        private String roleName;

        private String roleCode;

        private Long version;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionGroupRow {

        private String groupId;

        private String groupName;

        private List<PermissionRow> permissions;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionRow {

        private String permId;

        private String permCode;

        private String displayName;

        private Map<String, Boolean> roleGrants;

        private boolean system;
    }
}