package com.example.rbac.service;

import com.example.rbac.service.serviceImpl.CsvRoleImportServiceImpl;

import com.example.rbac.dto.response.CsvImportResponse;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.UserRole;
import com.example.rbac.exception.InvalidCsvImportException;
import com.example.rbac.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvRoleImportServiceImplTest {

    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private RoleLookupService roleLookupService;
    @Mock
    private RoleAuditService roleAuditService;
    @Mock
    private UserAssignmentSupportService userAssignmentSupportService;

    private CsvRoleImportServiceImpl service;
    private UUID tenantId;
    private UUID actorId;
    private UUID roleId;
    private Role role;

    @BeforeEach
    void setUp() {
        service = new CsvRoleImportServiceImpl(
                userRoleRepository,
                roleLookupService,
                roleAuditService,
                userAssignmentSupportService
        );
        tenantId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        role = new Role();
        role.setRoleId(roleId);
        role.setTenantId(tenantId);
        role.setRoleCode("HR_MANAGER");
        role.setRoleName("HR Manager");
        role.setActive(true);
    }

    @Test
    void importCsv_shouldImportValidRows() {
        UUID employeeId = UUID.randomUUID();
        LocalDate effective = LocalDate.now().plusDays(1);
        LocalDate expiry = effective.plusDays(30);

        String csv = "employeeId,roleCode,effectiveDate,expiryDate\n"
                + employeeId + ",HR_MANAGER," + effective + "," + expiry + "\n";

        MockMultipartFile file = csvFile(csv);

        when(roleLookupService.getAssignableRoleByCode(tenantId, "HR_MANAGER")).thenReturn(role);
        when(userRoleRepository.existsByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                tenantId, employeeId, roleId)).thenReturn(false);
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CsvImportResponse response = service.importCsv(tenantId, actorId, file);

        assertEquals(1, response.totalRows());
        assertEquals(1, response.importedCount());
        assertEquals(0, response.skippedCount());
        assertEquals(0, response.failedCount());
        assertTrue(response.errors().isEmpty());

        verify(userRoleRepository).save(argThat(item ->
                item.getUserId().equals(employeeId)
                        && item.getRoleId().equals(roleId)
                        && item.getTenantId().equals(tenantId)
                        && item.getAssignedBy().equals(actorId)
                        && item.getAssignedAt() != null
                        && item.isActive()
                        && !item.isPrimary()
        ));
        verify(roleAuditService).record(
                tenantId, actorId, employeeId, roleId,
                "CSV_ROLE_IMPORTED", "Imported from CSV");
    }

    @Test
    void importCsv_shouldSkipAlreadyAssignedRole() {
        UUID employeeId = UUID.randomUUID();
        LocalDate effective = LocalDate.now().plusDays(1);
        String csv = "employeeId,roleCode,effectiveDate,expiryDate\n"
                + employeeId + ",HR_MANAGER," + effective + ",\n";

        when(roleLookupService.getAssignableRoleByCode(tenantId, "HR_MANAGER")).thenReturn(role);
        when(userRoleRepository.existsByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                tenantId, employeeId, roleId)).thenReturn(true);

        CsvImportResponse response = service.importCsv(tenantId, actorId, csvFile(csv));

        assertEquals(1, response.totalRows());
        assertEquals(0, response.importedCount());
        assertEquals(1, response.skippedCount());
        assertEquals(0, response.failedCount());
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void importCsv_shouldContinueWhenOneRowIsInvalid() {
        UUID validEmployee = UUID.randomUUID();
        LocalDate effective = LocalDate.now().plusDays(2);

        String csv = "employeeId,roleCode,effectiveDate,expiryDate\n"
                + "not-a-uuid,HR_MANAGER," + effective + ",\n"
                + validEmployee + ",HR_MANAGER," + effective + ",\n";

        when(roleLookupService.getAssignableRoleByCode(tenantId, "HR_MANAGER")).thenReturn(role);
        when(userRoleRepository.existsByTenantIdAndUserIdAndRoleIdAndActiveTrue(
                tenantId, validEmployee, roleId)).thenReturn(false);
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CsvImportResponse response = service.importCsv(tenantId, actorId, csvFile(csv));

        assertEquals(2, response.totalRows());
        assertEquals(1, response.importedCount());
        assertEquals(0, response.skippedCount());
        assertEquals(1, response.failedCount());
        assertEquals(1, response.errors().size());
        assertEquals("not-a-uuid", response.errors().get(0).employeeId());
    }

    @Test
    void importCsv_shouldReportInvalidDateFormatAsRowError() {
        UUID employeeId = UUID.randomUUID();
        String csv = "employeeId,roleCode,effectiveDate,expiryDate\n"
                + employeeId + ",HR_MANAGER,15-09-2030,\n";

        CsvImportResponse response = service.importCsv(tenantId, actorId, csvFile(csv));

        assertEquals(1, response.failedCount());
        assertEquals("Date must use ISO format yyyy-MM-dd", response.errors().get(0).message());
    }

    @Test
    void importCsv_shouldRejectMissingRequiredHeader() {
        UUID employeeId = UUID.randomUUID();
        String csv = "employeeId,roleCode,effectiveDate\n"
                + employeeId + ",HR_MANAGER," + LocalDate.now().plusDays(1) + "\n";

        InvalidCsvImportException ex = assertThrows(
                InvalidCsvImportException.class,
                () -> service.importCsv(tenantId, actorId, csvFile(csv))
        );

        assertTrue(ex.getMessage().contains("CSV must contain headers"));
    }

    @Test
    void importCsv_shouldRejectNonCsvFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "roles.txt", "text/plain", "data".getBytes(StandardCharsets.UTF_8));

        InvalidCsvImportException ex = assertThrows(
                InvalidCsvImportException.class,
                () -> service.importCsv(tenantId, actorId, file)
        );

        assertEquals("Only .csv files are supported", ex.getMessage());
    }

    @Test
    void importCsv_shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "roles.csv", "text/csv", new byte[0]);

        InvalidCsvImportException ex = assertThrows(
                InvalidCsvImportException.class,
                () -> service.importCsv(tenantId, actorId, file)
        );

        assertTrue(ex.getMessage().contains("cannot be empty"));
    }

    private MockMultipartFile csvFile(String content) {
        return new MockMultipartFile(
                "file",
                "role-assignments.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
