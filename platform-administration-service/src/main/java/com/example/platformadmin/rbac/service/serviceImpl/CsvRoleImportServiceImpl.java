package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.platformadmin.rbac.service.RoleAuditService;
import com.example.platformadmin.rbac.service.UserAssignmentSupportService;
import com.example.platformadmin.rbac.dto.response.CsvImportError;
import com.example.platformadmin.rbac.dto.response.CsvImportResponse;
import com.example.platformadmin.rbac.entity.Role;
import com.example.platformadmin.rbac.entity.UserRole;
import com.example.platformadmin.rbac.exception.InvalidCsvImportException;
import com.example.platformadmin.rbac.repository.UserRoleRepository;
import com.example.platformadmin.rbac.service.CsvRoleImportService;
import com.example.platformadmin.rbac.service.RoleLookupService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CsvRoleImportServiceImpl implements CsvRoleImportService {

    private static final List<String> REQUIRED_HEADERS =
            List.of("employeeId", "roleCode", "effectiveDate", "expiryDate");

    private final UserRoleRepository userRoleRepository;
    private final RoleLookupService roleLookupService;
    private final RoleAuditService roleAuditService;
    private final UserAssignmentSupportService userAssignmentSupportService;

    public CsvRoleImportServiceImpl(
            UserRoleRepository userRoleRepository,
            RoleLookupService roleLookupService,
            RoleAuditService roleAuditService,
            UserAssignmentSupportService userAssignmentSupportService
    ) {
        this.userRoleRepository = userRoleRepository;
        this.roleLookupService = roleLookupService;
        this.roleAuditService = roleAuditService;
        this.userAssignmentSupportService = userAssignmentSupportService;
    }

    @Override
    public CsvImportResponse importCsv(UUID tenantId, UUID actorId, MultipartFile file) {
        validateFile(file);

        int totalRows = 0;
        int imported = 0;
        int skipped = 0;
        List<CsvImportError> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            validateHeaders(parser);

            for (CSVRecord record : parser) {
                totalRows++;
                try {
                    UUID employeeId = UUID.fromString(required(record, "employeeId"));
                    String roleCode = required(record, "roleCode");
                    LocalDate effectiveDate = LocalDate.parse(required(record, "effectiveDate"));
                    String expiryText = optional(record, "expiryDate");
                    LocalDate expiryDate = expiryText == null ? null : LocalDate.parse(expiryText);

                    validateDates(effectiveDate, expiryDate);
                    userAssignmentSupportService.validateAssignable(tenantId, employeeId);
                    Role role = roleLookupService.getAssignableRoleByCode(tenantId, roleCode);
                    UUID roleId = role.getId();

                    if (userRoleRepository.existsByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                            tenantId, employeeId, roleId)) {
                        skipped++;
                        continue;
                    }

                    UserRole assignment = new UserRole();
                    assignment.setTenantId(tenantId);
                    assignment.setUserId(employeeId);
                    assignment.setRoleId(roleId);
                    assignment.setPrimary(false);
                    assignment.setEffectiveDate(effectiveDate);
                    assignment.setExpiryDate(expiryDate);
                    assignment.setAssignedBy(actorId);
                    assignment.setAssignedAt(LocalDateTime.now());
                    assignment.setActive(true);

                    UserRole saved = userRoleRepository.save(assignment);
                    roleAuditService.record(
                            tenantId,
                            actorId,
                            saved.getUserId(),
                            saved.getRoleId(),
                            "CSV_ROLE_IMPORTED",
                            "Imported from CSV"
                    );
                    imported++;
                } catch (Exception ex) {
                    errors.add(new CsvImportError(
                            record.getRecordNumber() + 1,
                            safeGet(record, "employeeId"),
                            safeGet(record, "roleCode"),
                            cleanMessage(ex)
                    ));
                }
            }
        } catch (InvalidCsvImportException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidCsvImportException(
                    "Unable to process CSV: " + cleanMessage(ex));
        }

        return new CsvImportResponse(
                totalRows,
                imported,
                skipped,
                errors.size(),
                List.copyOf(errors)
        );
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidCsvImportException("CSV file is required and cannot be empty");
        }
        String originalName = file.getOriginalFilename();
        if (originalName != null
                && !originalName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new InvalidCsvImportException("Only .csv files are supported");
        }
    }

    private void validateHeaders(CSVParser parser) {
        for (String header : REQUIRED_HEADERS) {
            if (!parser.getHeaderMap().containsKey(header)) {
                throw new InvalidCsvImportException(
                        "CSV must contain headers: employeeId, roleCode, effectiveDate, expiryDate");
            }
        }
    }

    private void validateDates(LocalDate effectiveDate, LocalDate expiryDate) {
        if (effectiveDate.isBefore(LocalDate.now())) {
            throw new InvalidCsvImportException("effectiveDate cannot be in the past");
        }
        if (expiryDate != null && !expiryDate.isAfter(effectiveDate)) {
            throw new InvalidCsvImportException("expiryDate must be after effectiveDate");
        }
    }

    private String required(CSVRecord record, String name) {
        String value = optional(record, name);
        if (value == null) {
            throw new InvalidCsvImportException(name + " is required");
        }
        return value;
    }

    private String optional(CSVRecord record, String name) {
        String value = record.get(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String safeGet(CSVRecord record, String name) {
        try {
            String value = record.get(name);
            return value == null ? "" : value;
        } catch (Exception ignored) {
            return "";
        }
    }

    private String cleanMessage(Exception ex) {
        if (ex instanceof DateTimeParseException) {
            return "Date must use ISO format yyyy-MM-dd";
        }
        return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
    }
}
