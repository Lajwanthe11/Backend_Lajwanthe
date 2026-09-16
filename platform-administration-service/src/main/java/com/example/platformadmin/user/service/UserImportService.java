package com.example.platformadmin.user.service;

import com.example.platformadmin.user.dto.BulkUploadResponseDto;
import com.example.platformadmin.user.dto.BulkUserRequestDto;
import com.example.platformadmin.user.dto.ImportSummaryDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserImportService {

    BulkUploadResponseDto bulkUpload(List<BulkUserRequestDto> users);

    ImportSummaryDto importCsv(MultipartFile file) throws IOException;
}