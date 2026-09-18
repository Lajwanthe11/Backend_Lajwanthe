package com.example.rbac.service;

import com.example.rbac.dto.RoleAssignmentReportRow;

import java.util.List;

public interface RoleAssignmentExportService {
    byte[] toExcel(List<RoleAssignmentReportRow> rows);
    byte[] toPdf(List<RoleAssignmentReportRow> rows);
}
