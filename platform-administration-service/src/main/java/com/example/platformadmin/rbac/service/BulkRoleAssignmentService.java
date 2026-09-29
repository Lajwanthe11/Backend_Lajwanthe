package com.example.platformadmin.rbac.service;

import com.example.platformadmin.rbac.dto.response.BulkOperationResponse;
import com.example.platformadmin.rbac.dto.request.BulkRoleAssignmentRequest;
import com.example.platformadmin.rbac.dto.request.BulkRoleRevokeRequest;

import java.util.UUID;

public interface BulkRoleAssignmentService {
    BulkOperationResponse bulkAssign(UUID tenantId, UUID actorId, BulkRoleAssignmentRequest request);
    BulkOperationResponse bulkRevoke(UUID tenantId, UUID actorId, BulkRoleRevokeRequest request);
}
