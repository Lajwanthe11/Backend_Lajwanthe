package com.example.platformadmin.rbac.service;

import com.example.platformadmin.rbac.service.serviceImpl.RoleAssignmentExportServiceImpl;

import com.example.platformadmin.rbac.dto.response.RoleAssignmentReportRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoleAssignmentExportServiceImplTest {

    private RoleAssignmentExportServiceImpl service;
    private RoleAssignmentReportRow row;

    @BeforeEach
    void setUp() {
        service = new RoleAssignmentExportServiceImpl();
        row = new RoleAssignmentReportRow(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "HR_MANAGER",
                "HR Manager",
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                "ACTIVE",
                false,
                LocalDateTime.now()
        );
    }

    @Test
    void toExcel_shouldGenerateReadableWorkbook() throws Exception {
        byte[] bytes = service.toExcel(List.of(row));

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet("Role Assignments");
            assertNotNull(sheet);
            assertEquals("User Role ID", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("HR_MANAGER", sheet.getRow(1).getCell(3).getStringCellValue());
            assertEquals("ACTIVE", sheet.getRow(1).getCell(7).getStringCellValue());
        }
    }

    @Test
    void toPdf_shouldGeneratePdfBytes() {
        byte[] bytes = service.toPdf(List.of(row));

        assertNotNull(bytes);
        assertTrue(bytes.length > 5);
        String signature = new String(bytes, 0, 5, java.nio.charset.StandardCharsets.US_ASCII);
        assertEquals("%PDF-", signature);
    }

    @Test
    void exports_shouldSupportEmptyReport() {
        assertTrue(service.toExcel(List.of()).length > 0);
        assertTrue(service.toPdf(List.of()).length > 0);
    }
}
