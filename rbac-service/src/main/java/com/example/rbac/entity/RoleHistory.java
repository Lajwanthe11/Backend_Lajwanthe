package com.example.rbac.entity;

import com.example.common.abstracts.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "role_history")
public class RoleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Stored/queried as String to match findAllByRoleIdOrderByChangedAtDesc(String)
    // used in the service — role id is converted to String at the call site.
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "changed_by_user_id", nullable = false)
    private String changedByUserId;

    @Column(name = "changed_by_name", nullable = false, length = 150)
    private String changedByName;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "change_type", nullable = false, length = 40)
    private String changeType;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "old_value", length = 500)
    private String oldValue;

    @Column(name = "new_value", length = 500)
    private String newValue;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRoleId() { return roleId; }
    public void setRoleId(UUID roleId) { this.roleId = roleId; }

    public String getChangedByUserId() { return changedByUserId; }
    public void setChangedByUserId(String changedByUserId) { this.changedByUserId = changedByUserId; }

    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }

    public Instant getChangedAt() { return changedAt; }
    public void setChangedAt(Instant changedAt) { this.changedAt = changedAt; }

    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
}
