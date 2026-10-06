package com.example.platformadmin.rbac.service;

import com.example.platformadmin.rbac.dto.response.CsvImportResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface CsvRoleImportService {
    CsvImportResponse importCsv(UUID tenantId, UUID actorId, MultipartFile file);
}
