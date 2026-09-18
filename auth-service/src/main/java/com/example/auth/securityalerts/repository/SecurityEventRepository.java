package com.example.auth.securityalerts.repository;

import com.example.auth.securityalerts.dto.SecurityEventFilter;
import com.example.auth.securityalerts.entity.SecurityEvent;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long>, JpaSpecificationExecutor<SecurityEvent> {

    /** Events in the window that have not yet raised or joined an alert. */
    long countByTenantIdAndEventTypeAndSubjectKeyAndOccurredAtGreaterThanEqualAndAlertIdIsNull(
            String tenantId, EventType eventType, String subjectKey, LocalDateTime since);

    @Query("""
            select distinct e.ipAddress from SecurityEvent e
            where e.tenantId = :tenantId and e.username = :username and e.eventType in :types
              and e.ipAddress is not null and e.occurredAt >= :since
            """)
    List<String> findDistinctIpAddresses(@Param("tenantId") String tenantId,
                                         @Param("username") String username,
                                         @Param("types") Collection<EventType> types,
                                         @Param("since") LocalDateTime since);

    /** Attaches the events that crossed a policy threshold to the alert they raised, as its evidence. */
    @Modifying
    @Query("""
            update SecurityEvent e set e.alertId = :alertId
            where e.tenantId = :tenantId and e.eventType = :eventType and e.subjectKey = :subjectKey
              and e.occurredAt >= :since and e.alertId is null
            """)
    int linkToAlert(@Param("alertId") Long alertId,
                    @Param("tenantId") String tenantId,
                    @Param("eventType") EventType eventType,
                    @Param("subjectKey") String subjectKey,
                    @Param("since") LocalDateTime since);

    Page<SecurityEvent> findByAlertId(Long alertId, Pageable pageable);

    /** Event log filters. tenantId null means every tenant. The date range is [from, toExclusive). */
    static Specification<SecurityEvent> matching(String tenantId, SecurityEventFilter filter,
                                                 LocalDateTime from, LocalDateTime toExclusive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tenantId != null) {
                predicates.add(cb.equal(root.get("tenantId"), tenantId));
            }
            if (StringUtils.hasText(filter.getUsername())) {
                predicates.add(cb.equal(root.get("username"), filter.getUsername().trim()));
            }
            if (filter.getEventType() != null) {
                predicates.add(cb.equal(root.get("eventType"), filter.getEventType()));
            }
            if (filter.getSourceModule() != null) {
                predicates.add(cb.equal(root.get("sourceModule"), filter.getSourceModule()));
            }
            if (StringUtils.hasText(filter.getIpAddress())) {
                predicates.add(cb.equal(root.get("ipAddress"), filter.getIpAddress().trim()));
            }
            if (filter.getAlertRaised() != null) {
                predicates.add(filter.getAlertRaised()
                        ? cb.isNotNull(root.get("alertId"))
                        : cb.isNull(root.get("alertId")));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
            }
            if (toExclusive != null) {
                predicates.add(cb.lessThan(root.get("occurredAt"), toExclusive));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
