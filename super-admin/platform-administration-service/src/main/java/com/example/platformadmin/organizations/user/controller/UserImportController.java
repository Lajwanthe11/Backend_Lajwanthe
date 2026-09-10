package com.example.platformadmin.organizations.user.controller;

import com.example.platformadmin.organizations.user.dto.BulkUploadResponseDto;
import com.example.platformadmin.organizations.user.dto.BulkUserRequestDto;
import com.example.platformadmin.organizations.user.dto.ImportSummaryDto;
import com.example.platformadmin.organizations.user.service.UserImportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserImportController {

    private final UserImportService userImportService;

    public UserImportController(UserImportService userImportService) {
        this.userImportService = userImportService;
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<BulkUploadResponseDto> bulkUpload(
            @RequestBody List<BulkUserRequestDto> users) {

        BulkUploadResponseDto response =
                userImportService.bulkUpload(users);

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportSummaryDto> importUsers(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        ImportSummaryDto response =
                userImportService.importCsv(file);

        return ResponseEntity.ok(response);
    }
}