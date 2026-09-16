package com.example.rbac.dto;

import java.time.Instant;

public class RoleHistoryDto {

    private String changedByName;
    private Instant changedAt;
    private String changeType;
    private String fieldName;
    private String oldValue;
    private String newValue;

    public RoleHistoryDto(String changedByName, Instant changedAt, String changeType,
                          String fieldName, String oldValue, String newValue) {
        this.changedByName = changedByName;
        this.changedAt = changedAt;
        this.changeType = changeType;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
