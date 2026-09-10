package com.example.rbac.service;

import com.example.rbac.entity.Role;

import java.util.List;

/**
 * Generates the exported file bytes. Implement with Apache POI for "xlsx"
 * and OpenPDF/iText for "pdf". Kept as a separate service so RoleServiceImpl
 * stays focused on tenant scoping and doesn't own file-format concerns.
 */
public interface RoleExportService {
    byte[] export(List<Role> roles, String format);
}