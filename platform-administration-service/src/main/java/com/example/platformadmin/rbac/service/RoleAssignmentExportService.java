package com.example.platformadmin.rbac.service;

import com.example.platformadmin.rbac.dto.response.RoleAssignmentReportRow;

import java.util.List;

public interface RoleAssignmentExportService {
    byte[] toExcel(List<RoleAssignmentReportRow> rows);
    byte[] toPdf(List<RoleAssignmentReportRow> rows);
}
