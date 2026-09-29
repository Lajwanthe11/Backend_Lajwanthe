package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.platformadmin.rbac.dto.response.ModuleAccess;
import com.example.platformadmin.rbac.dto.response.UiPermissionResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Converts raw permissions into frontend-friendly module and feature flags.
@Service
public class FeatureVisibilityMapper {

    public UiPermissionResponse buildResponse(Set<String> permissions) {
        Map<String, ModuleAccess> modules = new LinkedHashMap<>();

        boolean userMgmtAccess = permissions.contains("USER_READ");
        Map<String, Boolean> userMgmtFeatures = new LinkedHashMap<>();
        userMgmtFeatures.put("CREATE_USER", permissions.contains("USER_CREATE"));
        userMgmtFeatures.put("UPDATE_USER", permissions.contains("USER_UPDATE"));
        userMgmtFeatures.put("DELETE_USER", permissions.contains("USER_DELETE"));
        modules.put("USER_MGMT", new ModuleAccess(userMgmtAccess, userMgmtFeatures));

        boolean reportsAccess = permissions.contains("REPORT_VIEW") || permissions.contains("REPORT_EXPORT");
        Map<String, Boolean> reportFeatures = new LinkedHashMap<>();
        reportFeatures.put("VIEW_REPORT", permissions.contains("REPORT_VIEW"));
        reportFeatures.put("EXPORT_REPORT", permissions.contains("REPORT_EXPORT"));
        modules.put("REPORTS", new ModuleAccess(reportsAccess, reportFeatures));

        boolean securityAccess = permissions.contains("SECURITY_EVENTS_VIEW");
        Map<String, Boolean> securityFeatures = new LinkedHashMap<>();
        securityFeatures.put("VIEW_SECURITY_EVENTS", securityAccess);
        modules.put("SECURITY", new ModuleAccess(securityAccess, securityFeatures));

        List<String> menuItems = new ArrayList<>();
        if (userMgmtAccess)
            menuItems.add("USERS");
        if (reportsAccess)
            menuItems.add("REPORTS");
        if (securityAccess)
            menuItems.add("SECURITY_EVENTS");

        List<String> dashboardWidgets = new ArrayList<>();
        if (userMgmtAccess)
            dashboardWidgets.add("USER_SUMMARY");
        if (reportsAccess)
            dashboardWidgets.add("REPORT_SUMMARY");

        return new UiPermissionResponse(List.copyOf(permissions), modules, menuItems, dashboardWidgets);
    }
}
