package com.example.platformadmin.organizations.user.service;

import com.example.platformadmin.organizations.user.dto.BulkUploadResponseDto;
import com.example.platformadmin.organizations.user.dto.BulkUserRequestDto;
import com.example.platformadmin.organizations.user.dto.ImportSummaryDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserImportService {

    BulkUploadResponseDto bulkUpload(List<BulkUserRequestDto> users);

    ImportSummaryDto importCsv(MultipartFile file) throws IOException;
}