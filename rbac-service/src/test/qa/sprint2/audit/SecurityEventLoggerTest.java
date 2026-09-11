package com.example.qa.sprint2.audit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.rbac.entity.SecurityEvent;
import com.example.rbac.enums.SecurityEventType;
import com.example.rbac.repository.SecurityEventRepository;
import com.example.rbac.service.SecurityEventLogger;

@ExtendWith(MockitoExtension.class)
class SecurityEventLoggerTest {

    @Mock
    private SecurityEventRepository repository;

    @InjectMocks
    private SecurityEventLogger securityEventLogger;

    @Test
    void logAccessDenied_shouldSaveSecurityEvent() {

        securityEventLogger.logAccessDenied(
                "user-123",
                "tenant-001",
                "USER_DELETE",
                "/api/v1/users/123",
                "192.168.1.10"
        );

        ArgumentCaptor<SecurityEvent> captor =
                ArgumentCaptor.forClass(SecurityEvent.class);

        verify(repository).save(captor.capture());

        SecurityEvent event = captor.getValue();

        assertEquals("user-123", event.getUserId());
        assertEquals("tenant-001", event.getTenantId());
        assertEquals("USER_DELETE",
                event.getRequestedPermission());

        assertEquals("/api/v1/users/123",
                event.getEndpoint());

        assertEquals("192.168.1.10",
                event.getIpAddress());

        assertEquals(
                SecurityEventType.ACCESS_DENIED,
                event.getEventType()
        );

        assertNotNull(event.getTimestamp());
    }

    @Test
    void logAccessDenied_shouldCreateTimestamp() {

        Instant before = Instant.now();

        securityEventLogger.logAccessDenied(
                "user-123",
                "tenant-001",
                "USER_UPDATE",
                "/api/v1/users/123",
                "10.0.0.1"
        );

        Instant after = Instant.now();

        ArgumentCaptor<SecurityEvent> captor =
                ArgumentCaptor.forClass(SecurityEvent.class);

        verify(repository).save(captor.capture());

        SecurityEvent event = captor.getValue();

        assertNotNull(event.getTimestamp());

        assertFalse(event.getTimestamp().isBefore(before));
        assertFalse(event.getTimestamp().isAfter(after));
    }

    @Test
    void logAccessDenied_shouldSaveExactlyOneEvent() {

        securityEventLogger.logAccessDenied(
                "user-456",
                "tenant-002",
                "REPORT_EXPORT",
                "/api/v1/reports",
                "127.0.0.1"
        );

        verify(repository, times(1))
                .save(any(SecurityEvent.class));
    }

    @Test
    void logAccessDenied_shouldRecordDifferentUsersSeparately() {

        securityEventLogger.logAccessDenied(
                "user-001",
                "tenant-001",
                "USER_DELETE",
                "/api/v1/users/1",
                "10.0.0.1"
        );

        securityEventLogger.logAccessDenied(
                "user-002",
                "tenant-001",
                "USER_DELETE",
                "/api/v1/users/2",
                "10.0.0.2"
        );

        verify(repository, times(2))
                .save(any(SecurityEvent.class));
    }

    @Test
    void logAccessDenied_shouldRecordTenantId() {

        securityEventLogger.logAccessDenied(
                "user-100",
                "tenant-A",
                "ADMIN_ACCESS",
                "/api/v1/admin",
                "172.16.0.5"
        );

        ArgumentCaptor<SecurityEvent> captor =
                ArgumentCaptor.forClass(SecurityEvent.class);

        verify(repository).save(captor.capture());

        SecurityEvent event = captor.getValue();

        assertEquals("tenant-A", event.getTenantId());
    }

    @Test
    void logAccessDenied_shouldRecordRequestedPermission() {

        securityEventLogger.logAccessDenied(
                "user-100",
                "tenant-A",
                "PAYROLL_VIEW",
                "/api/v1/payroll",
                "172.16.0.5"
        );

        ArgumentCaptor<SecurityEvent> captor =
                ArgumentCaptor.forClass(SecurityEvent.class);

        verify(repository).save(captor.capture());

        SecurityEvent event = captor.getValue();

        assertEquals(
                "PAYROLL_VIEW",
                event.getRequestedPermission()
        );
    }
}