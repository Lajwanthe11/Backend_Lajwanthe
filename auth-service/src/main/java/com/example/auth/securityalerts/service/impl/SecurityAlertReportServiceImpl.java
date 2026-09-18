package com.example.auth.securityalerts.service.impl;

import com.example.auth.securityalerts.dto.DateRange;
import com.example.auth.securityalerts.dto.SecurityAlertFilter;
import com.example.auth.securityalerts.dto.SecurityAlertSummaryResponse;
import com.example.auth.securityalerts.dto.SecurityAlertTrendResponse;
import com.example.auth.securityalerts.entity.SecurityAlert;
import com.example.auth.securityalerts.entity.SecurityAlert.AlertType;
import com.example.auth.securityalerts.entity.SecurityAlert.Severity;
import com.example.auth.securityalerts.entity.SecurityAlert.Status;
import com.example.auth.securityalerts.repository.SecurityAlertRepository;
import com.example.auth.securityalerts.service.SecurityAlertAccessService;
import com.example.auth.securityalerts.service.SecurityAlertReportService;
import com.example.common.exception.BadRequestException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
public class SecurityAlertReportServiceImpl implements SecurityAlertReportService {

    private static final Logger log = LoggerFactory.getLogger(SecurityAlertReportServiceImpl.class);

    private static final int MAX_TREND_DAYS = 90;

    /** Critical alerts are prioritized: most severe first, newest first within a severity. */
    private static final Sort PRIORITY_SORT = Sort.by(Sort.Order.desc("severityRank"), Sort.Order.desc("alertTime"));

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final String[] CSV_HEADER = {
            "Alert ID", "Alert Type", "Event Type", "Severity", "Status", "Alert Time", "Last Occurred", "Occurrences",
            "Organization", "User ID", "Username", "Company", "Department", "Device", "IP Address", "Location",
            "Title", "Description", "Resolution Status", "Resolution Remarks", "Resolution Time", "Resolved By",
            "Last Updated", "Updated By"
    };

    private final SecurityAlertRepository alertRepository;
    private final SecurityAlertAccessService access;
    private final EntityManager entityManager;
    private final int exportMaxRows;

    public SecurityAlertReportServiceImpl(SecurityAlertRepository alertRepository,
                                          SecurityAlertAccessService access,
                                          EntityManager entityManager,
                                          @Value("${app.security-alerts.export-max-rows:10000}") int exportMaxRows) {
        this.alertRepository = alertRepository;
        this.access = access;
        this.entityManager = entityManager;
        this.exportMaxRows = exportMaxRows;
    }

    // ---------------------------------------------------------------
    // Alert Summary
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public SecurityAlertSummaryResponse getSummary(SecurityAlertFilter filter) {
        // The summary is itself a breakdown by status and severity, so those two filters do not apply.
        filter.setStatus(null);
        filter.setSeverity(null);
        String tenantId = access.resolveTenantScope(filter.getTenantId());
        DateRange range = DateRange.of(filter.getFrom(), filter.getTo());
        Specification<SecurityAlert> spec = SecurityAlertRepository.matching(tenantId, filter, range.from(), range.toExclusive());

        Map<Status, Long> byStatus = new EnumMap<>(Status.class);
        Map<Severity, Long> bySeverity = new EnumMap<>(Severity.class);
        long total = 0;
        long activeCritical = 0;
        for (Object[] row : countGroupedBy(spec, "status", "severity")) {
            Status status = (Status) row[0];
            Severity severity = (Severity) row[1];
            long count = (Long) row[2];
            total += count;
            byStatus.merge(status, count, Long::sum);
            bySeverity.merge(severity, count, Long::sum);
            if (severity == Severity.CRITICAL && status.isActive()) {
                activeCritical += count;
            }
        }

        Map<AlertType, Long> byType = new EnumMap<>(AlertType.class);
        for (AlertType type : AlertType.values()) {
            byType.put(type, 0L);
        }
        for (Object[] row : countGroupedBy(spec, "alertType")) {
            byType.put((AlertType) row[0], (Long) row[1]);
        }

        // "Resolved Today" goes by resolution time, whatever the date filter on alert time says.
        long resolvedToday = alertRepository.count(SecurityAlertRepository.matching(tenantId, filter, null, null)
                .and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("resolutionTime"), LocalDate.now().atStartOfDay())));

        // "Audit Information": the most recently changed alert in scope.
        Optional<SecurityAlert> lastUpdated = alertRepository
                .findAll(spec, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "updatedAt")))
                .stream().findFirst();

        return new SecurityAlertSummaryResponse(
                total,
                byStatus.getOrDefault(Status.OPEN, 0L),
                byStatus.getOrDefault(Status.ACKNOWLEDGED, 0L),
                byStatus.getOrDefault(Status.RESOLVED, 0L),
                byStatus.getOrDefault(Status.CLOSED, 0L),
                bySeverity.getOrDefault(Severity.CRITICAL, 0L),
                bySeverity.getOrDefault(Severity.HIGH, 0L),
                bySeverity.getOrDefault(Severity.MEDIUM, 0L),
                bySeverity.getOrDefault(Severity.LOW, 0L),
                activeCritical,
                resolvedToday,
                byType,
                lastUpdated.map(SecurityAlert::getUpdatedAt).orElse(null),
                lastUpdated.map(SecurityAlert::getUpdatedBy).orElse(null));
    }

    // ---------------------------------------------------------------
    // Alert Trend
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<SecurityAlertTrendResponse> getTrend(String tenantId, int days) {
        if (days < 1 || days > MAX_TREND_DAYS) {
            throw new BadRequestException("days must be between 1 and " + MAX_TREND_DAYS);
        }
        String scope = access.resolveTenantScope(tenantId);
        LocalDate firstDay = LocalDate.now().minusDays(days - 1L);

        TypedQuery<Object[]> query = entityManager.createQuery(
                "select cast(a.alertTime as LocalDate), a.severity, count(a) from SecurityAlert a"
                        + " where a.alertTime >= :start" + (scope != null ? " and a.tenantId = :tenantId" : "")
                        + " group by cast(a.alertTime as LocalDate), a.severity",
                Object[].class);
        query.setParameter("start", firstDay.atStartOfDay());
        if (scope != null) {
            query.setParameter("tenantId", scope);
        }

        Map<LocalDate, long[]> perDay = new TreeMap<>();
        for (int i = 0; i < days; i++) {
            perDay.put(firstDay.plusDays(i), new long[Severity.values().length]);
        }
        for (Object[] row : query.getResultList()) {
            long[] counts = perDay.get(toLocalDate(row[0]));
            if (counts != null) {
                counts[((Severity) row[1]).ordinal()] += (Long) row[2];
            }
        }

        return perDay.entrySet().stream()
                .map(day -> {
                    long[] c = day.getValue();
                    long low = c[Severity.LOW.ordinal()];
                    long medium = c[Severity.MEDIUM.ordinal()];
                    long high = c[Severity.HIGH.ordinal()];
                    long critical = c[Severity.CRITICAL.ordinal()];
                    return new SecurityAlertTrendResponse(day.getKey(), low + medium + high + critical, low, medium, high, critical);
                })
                .toList();
    }

    // ---------------------------------------------------------------
    // Export Report
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ExportFile exportAlerts(SecurityAlertFilter filter) {
        String tenantId = access.resolveTenantScope(filter.getTenantId());
        DateRange range = DateRange.of(filter.getFrom(), filter.getTo());
        Page<SecurityAlert> page = alertRepository.findAll(
                SecurityAlertRepository.matching(tenantId, filter, range.from(), range.toExclusive()),
                PageRequest.of(0, exportMaxRows, PRIORITY_SORT));

        StringBuilder csv = new StringBuilder("﻿"); // byte order mark, so Excel opens the file as UTF-8
        appendCsvRow(csv, (Object[]) CSV_HEADER);
        for (SecurityAlert a : page.getContent()) {
            appendCsvRow(csv, a.getAlertCode(), a.getAlertType(), a.getEventType(), a.getSeverity(), a.getStatus(),
                    a.getAlertTime(), a.getLastOccurredAt(), a.getOccurrenceCount(), a.getTenantId(), a.getUserId(),
                    a.getUsername(), a.getCompanyName(), a.getDepartmentName(),
                    a.getDeviceName() != null ? a.getDeviceName() : a.getDeviceId(), a.getIpAddress(), a.getLocation(),
                    a.getTitle(), a.getDescription(), a.getResolutionStatus(), a.getResolutionRemarks(),
                    a.getResolutionTime(), a.getResolvedBy(), a.getUpdatedAt(), a.getUpdatedBy());
        }

        int rows = page.getNumberOfElements();
        log.info("Security alert report exported by '{}': {} of {} matching alerts. Filters: {}",
                access.currentActor().username(), rows, page.getTotalElements(), filter);
        String fileName = "security-alerts-" + LocalDateTime.now().format(FILE_TIME_FORMAT) + ".csv";
        return new ExportFile(fileName, csv.toString().getBytes(StandardCharsets.UTF_8), rows, page.getTotalElements());
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private List<Object[]> countGroupedBy(Specification<SecurityAlert> spec, String... attributes) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<SecurityAlert> root = query.from(SecurityAlert.class);

        List<Selection<?>> selections = new ArrayList<>();
        List<Expression<?>> grouping = new ArrayList<>();
        for (String attribute : attributes) {
            Path<Object> path = root.get(attribute);
            selections.add(path);
            grouping.add(path);
        }
        selections.add(cb.count(root));

        query.multiselect(selections);
        Predicate predicate = spec.toPredicate(root, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }
        query.groupBy(grouping);
        return entityManager.createQuery(query).getResultList();
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalDate();
        }
        throw new IllegalStateException("Unexpected date value from trend query: " + value);
    }

    private static void appendCsvRow(StringBuilder csv, Object... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(csvCell(values[i]));
        }
        csv.append("\r\n");
    }

    private static String csvCell(Object value) {
        if (value == null) {
            return "";
        }
        String text = value instanceof LocalDateTime time ? time.format(TIME_FORMAT) : value.toString();
        // A cell starting with = + - @ is run as a formula by spreadsheet apps (CSV injection).
        if (!text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }
        if (text.indexOf(',') >= 0 || text.indexOf('"') >= 0 || text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0) {
            text = '"' + text.replace("\"", "\"\"") + '"';
        }
        return text;
    }
}
