package com.example.rbac.service;

import com.example.rbac.dto.BulkOperationResponse;
import com.example.rbac.dto.BulkRoleAssignmentRequest;
import com.example.rbac.dto.BulkRoleRevokeRequest;

import java.util.UUID;

public interface BulkRoleAssignmentService {
    BulkOperationResponse bulkAssign(UUID tenantId, UUID actorId, BulkRoleAssignmentRequest request);
    BulkOperationResponse bulkRevoke(UUID tenantId, UUID actorId, BulkRoleRevokeRequest request);
}
