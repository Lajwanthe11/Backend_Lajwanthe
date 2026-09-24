package com.example.platformadmin.organizations.organization.enums;

public enum OrganizationType {
    ENTERPRISE("Enterprise"),
    MID_MARKET("Mid-Market"),
    SMALL_BUSINESS("Small Business (SMB)"),
    STARTUP("Startup"),
    HOLDING_COMPANY("Holding Company / Group"),
    GOVERNMENT("Government / Public Sector"),
    NON_PROFIT("Non-Profit");

    private final String displayName;

    OrganizationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}