package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.platformadmin.rbac.entity.Role;
import com.example.platformadmin.rbac.service.RoleExportService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class RoleExportServiceImpl implements RoleExportService {

    @Override
    public byte[] export(List<Role> roles, String format) {
        if (roles == null) {
            return new byte[0];
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Role ID,Role Name,Role Code,Tenant ID\n");
        for (Role role : roles) {
            sb.append(role.getId() != null ? role.getId() : "").append(",")
              .append(role.getRoleName() != null ? role.getRoleName() : "").append(",")
              .append(role.getRoleCode() != null ? role.getRoleCode() : "").append(",")
              .append(role.getTenantId() != null ? role.getTenantId() : "").append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
