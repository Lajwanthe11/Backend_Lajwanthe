package com.example.rbac.service;

import com.example.rbac.dto.response.BulkOperationResponse;
import com.example.rbac.dto.request.BulkRoleAssignmentRequest;
import com.example.rbac.dto.request.BulkRoleRevokeRequest;

import java.util.UUID;

public interface BulkRoleAssignmentService {
    BulkOperationResponse bulkAssign(UUID tenantId, UUID actorId, BulkRoleAssignmentRequest request);
    BulkOperationResponse bulkRevoke(UUID tenantId, UUID actorId, BulkRoleRevokeRequest request);
}
