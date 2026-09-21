package com.example.auth.securityalerts.repository;

import com.example.auth.securityalerts.dto.SecurityAlertFilter;
import com.example.auth.securityalerts.entity.SecurityAlert;
import com.example.auth.securityalerts.entity.SecurityAlert.Status;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long>, JpaSpecificationExecutor<SecurityAlert> {

    /** The still-active alert that a new event for the same subject should merge into, if any. */
    Optional<SecurityAlert> findFirstByTenantIdAndEventTypeAndSubjectKeyAndStatusInAndLastOccurredAtGreaterThanEqualOrderByLastOccurredAtDesc(
            String tenantId, EventType eventType, String subjectKey, Collection<Status> statuses, LocalDateTime since);

    Page<SecurityAlert> findByTenantIdAndUsername(String tenantId, String username, Pageable pageable);

    /**
     * Alert list filters. tenantId null means every tenant, which only a Super Administrator gets.
     * The date range is [from, toExclusive) on alert time; either end may be null.
     */
    static Specification<SecurityAlert> matching(String tenantId, SecurityAlertFilter filter,
                                                 LocalDateTime from, LocalDateTime toExclusive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tenantId != null) {
                predicates.add(cb.equal(root.get("tenantId"), tenantId));
            }
            if (StringUtils.hasText(filter.getSearch())) {
                String like = "%" + escapeLike(filter.getSearch().trim().toLowerCase()) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("alertCode")), like, '\\'),
                        cb.like(cb.lower(root.get("username")), like, '\\'),
                        cb.like(cb.lower(root.get("title")), like, '\\'),
                        cb.like(cb.lower(root.get("ipAddress")), like, '\\')));
            }
            if (filter.getCompanyId() != null) {
                predicates.add(cb.equal(root.get("companyId"), filter.getCompanyId()));
            }
            if (filter.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("departmentId"), filter.getDepartmentId()));
            }
            if (filter.getAlertType() != null) {
                predicates.add(cb.equal(root.get("alertType"), filter.getAlertType()));
            }
            if (filter.getEventType() != null) {
                predicates.add(cb.equal(root.get("eventType"), filter.getEventType()));
            }
            if (filter.getSeverity() != null && !filter.getSeverity().isEmpty()) {
                predicates.add(root.get("severity").in(filter.getSeverity()));
            }
            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                predicates.add(root.get("status").in(filter.getStatus()));
            }
            if (StringUtils.hasText(filter.getUsername())) {
                predicates.add(cb.equal(root.get("username"), filter.getUsername().trim()));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("alertTime"), from));
            }
            if (toExclusive != null) {
                predicates.add(cb.lessThan(root.get("alertTime"), toExclusive));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
