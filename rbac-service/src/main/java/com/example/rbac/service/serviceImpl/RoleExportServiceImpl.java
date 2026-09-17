package com.example.rbac.service.serviceImpl;

import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.service.RoleExportService;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class RoleExportServiceImpl implements RoleExportService {

    @Override
    public byte[] export(List<Role> roles, String format) {
        // Only CSV is implemented; "format" is accepted for forward
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            writer.println("RoleName,RoleCode,RoleType,Status,PermissionCount,Permissions");
            for (Role role : roles) {
                String permissionCodes = role.getPermissions().stream()
                        .map(Permission::getPermissionCode)
                        .collect(Collectors.joining(";"));
                writer.println(String.join(",",
                        escape(role.getRoleName()),
                        escape(role.getRoleCode()),
                        String.valueOf(role.getRoleType()),
                        escape(role.getStatus()),
                        String.valueOf(role.getPermissions().size()),
                        escape(permissionCodes)));
            }
        }
        return out.toByteArray();
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
