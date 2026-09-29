package com.example.platformadmin.rbac.dto.response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for the Permission Matrix.
 *
 * Contains the roles displayed as matrix columns and the
 * permission groups displayed as matrix rows.
 */
public class PermissionMatrixResponse {

    // Roles displayed as columns in the Permission Matrix.
    private List<RoleColumn> roles;

    // Permission groups displayed as sections in the Permission Matrix.
    private List<PermissionGroupRow> permissionGroups;

    public PermissionMatrixResponse() {
    }

    public PermissionMatrixResponse(
            List<RoleColumn> roles,
            List<PermissionGroupRow> permissionGroups) {
        this.roles = roles;
        this.permissionGroups = permissionGroups;
    }

    public List<RoleColumn> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleColumn> roles) {
        this.roles = roles;
    }

    public List<PermissionGroupRow> getPermissionGroups() {
        return permissionGroups;
    }

    public void setPermissionGroups(
            List<PermissionGroupRow> permissionGroups) {
        this.permissionGroups = permissionGroups;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for PermissionMatrixResponse.
     */
    public static class Builder {

        private List<RoleColumn> roles;
        private List<PermissionGroupRow> permissionGroups;

        public Builder roles(List<RoleColumn> roles) {
            this.roles = roles;
            return this;
        }

        public Builder permissionGroups(
                List<PermissionGroupRow> permissionGroups) {
            this.permissionGroups = permissionGroups;
            return this;
        }

        public PermissionMatrixResponse build() {
            return new PermissionMatrixResponse(
                    roles,
                    permissionGroups);
        }
    }

    /**
     * Represents one role displayed as a column in the matrix.
     */
    public static class RoleColumn {

        // Unique ID of the role.
        private UUID roleId;

        // Display name of the role.
        private String roleName;

        // Unique/business code of the role.
        private String roleCode;

        public RoleColumn() {
        }

        public RoleColumn(
                UUID roleId,
                String roleName,
                String roleCode) {
            this.roleId = roleId;
            this.roleName = roleName;
            this.roleCode = roleCode;
        }

        public UUID getRoleId() {
            return roleId;
        }

        public void setRoleId(UUID roleId) {
            this.roleId = roleId;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }

        public String getRoleCode() {
            return roleCode;
        }

        public void setRoleCode(String roleCode) {
            this.roleCode = roleCode;
        }

        public static RoleColumnBuilder builder() {
            return new RoleColumnBuilder();
        }

        /**
         * Builder for RoleColumn.
         */
        public static class RoleColumnBuilder {

            private UUID roleId;
            private String roleName;
            private String roleCode;

            public RoleColumnBuilder roleId(UUID roleId) {
                this.roleId = roleId;
                return this;
            }

            public RoleColumnBuilder roleName(String roleName) {
                this.roleName = roleName;
                return this;
            }

            public RoleColumnBuilder roleCode(String roleCode) {
                this.roleCode = roleCode;
                return this;
            }

            public RoleColumn build() {
                return new RoleColumn(
                        roleId,
                        roleName,
                        roleCode);
            }
        }
    }

    /**
     * Represents one permission group in the matrix.
     *
     * A permission group can contain multiple permissions.
     */
    public static class PermissionGroupRow {

        // Unique ID of the permission group.
        private String groupId;

        // Display name of the permission group.
        private String groupName;

        // Permissions belonging to this group.
        private List<PermissionRow> permissions;

        public PermissionGroupRow() {
        }

        public PermissionGroupRow(
                String groupId,
                String groupName,
                List<PermissionRow> permissions) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.permissions = permissions;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public List<PermissionRow> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<PermissionRow> permissions) {
            this.permissions = permissions;
        }

        public static PermissionGroupRowBuilder builder() {
            return new PermissionGroupRowBuilder();
        }

        /**
         * Builder for PermissionGroupRow.
         */
        public static class PermissionGroupRowBuilder {

            private String groupId;
            private String groupName;
            private List<PermissionRow> permissions;

            public PermissionGroupRowBuilder groupId(String groupId) {
                this.groupId = groupId;
                return this;
            }

            public PermissionGroupRowBuilder groupName(String groupName) {
                this.groupName = groupName;
                return this;
            }

            public PermissionGroupRowBuilder permissions(
                    List<PermissionRow> permissions) {
                this.permissions = permissions;
                return this;
            }

            public PermissionGroupRow build() {
                return new PermissionGroupRow(
                        groupId,
                        groupName,
                        permissions);
            }
        }
    }

    /**
     * Represents one permission row in the matrix.
     *
     * roleGrants contains the grant status for each role.
     */
    public static class PermissionRow {

        // Permission UUID represented as a String in the API response.
        private String permId;

        // Unique permission code.
        private String permCode;

        // Human-readable permission name.
        private String displayName;

        // Maps role ID to its grant status.
        //
        // true = role has the permission.
        // false = role does not have the permission.
        private Map<String, Boolean> roleGrants;

        // Indicates whether this permission is a system permission.
        private boolean system;

        public PermissionRow() {
        }

        public PermissionRow(
                String permId,
                String permCode,
                String displayName,
                Map<String, Boolean> roleGrants,
                boolean system) {
            this.permId = permId;
            this.permCode = permCode;
            this.displayName = displayName;
            this.roleGrants = roleGrants;
            this.system = system;
        }

        public String getPermId() {
            return permId;
        }

        public void setPermId(String permId) {
            this.permId = permId;
        }

        public String getPermCode() {
            return permCode;
        }

        public void setPermCode(String permCode) {
            this.permCode = permCode;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public Map<String, Boolean> getRoleGrants() {
            return roleGrants;
        }

        public void setRoleGrants(
                Map<String, Boolean> roleGrants) {
            this.roleGrants = roleGrants;
        }

        public boolean isSystem() {
            return system;
        }

        public void setSystem(boolean system) {
            this.system = system;
        }

        public static PermissionRowBuilder builder() {
            return new PermissionRowBuilder();
        }

        /**
         * Builder for PermissionRow.
         */
        public static class PermissionRowBuilder {

            private String permId;
            private String permCode;
            private String displayName;
            private Map<String, Boolean> roleGrants;
            private boolean system;

            public PermissionRowBuilder permId(String permId) {
                this.permId = permId;
                return this;
            }

            public PermissionRowBuilder permCode(String permCode) {
                this.permCode = permCode;
                return this;
            }

            public PermissionRowBuilder displayName(String displayName) {
                this.displayName = displayName;
                return this;
            }

            public PermissionRowBuilder roleGrants(
                    Map<String, Boolean> roleGrants) {
                this.roleGrants = roleGrants;
                return this;
            }

            public PermissionRowBuilder system(boolean system) {
                this.system = system;
                return this;
            }

            public PermissionRow build() {
                return new PermissionRow(
                        permId,
                        permCode,
                        displayName,
                        roleGrants,
                        system);
            }
        }
    }
}
