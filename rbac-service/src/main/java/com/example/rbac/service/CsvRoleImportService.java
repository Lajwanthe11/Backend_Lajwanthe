package com.example.rbac.service;

import com.example.rbac.dto.CsvImportResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface CsvRoleImportService {
    CsvImportResponse importCsv(UUID tenantId, UUID actorId, MultipartFile file);
}
