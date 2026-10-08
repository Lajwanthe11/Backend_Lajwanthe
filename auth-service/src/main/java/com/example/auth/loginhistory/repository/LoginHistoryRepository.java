package com.example.auth.loginhistory.repository;

import com.example.auth.loginhistory.dto.LoginHistorySearchCriteria;
import com.example.auth.loginhistory.entity.LoginHistory;
import com.example.auth.loginhistory.entity.LoginHistory.AuthenticationMethod;
import com.example.auth.loginhistory.entity.LoginHistory.LoginStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Repository for Login History, plus the composable query criteria the screen filters by.
 * None of the criteria filter on tenant: Hibernate adds that to every query through BaseEntity's @TenantId.
 */
@Repository
public interface LoginHistoryRepository
        extends JpaRepository<LoginHistory, Long>, JpaSpecificationExecutor<LoginHistory> {

    Optional<LoginHistory> findBySessionId(String sessionId);

    /** The caller's most recent login attempt (success or failure) — backs the "my status" widget. */
    Optional<LoginHistory> findFirstByUsernameOrderByLoginTimeDesc(String username);

    /**
     * Successful logins still open (no logoutTime) whose session has already expired.
     * Closed with {@code LogoutType.AUTO} via {@code LoginHistoryService#autoCloseExpiredSessions()}.
     */
    List<LoginHistory> findByStatusAndLogoutTimeIsNullAndSessionExpiresAtBefore(
            LoginStatus status, LocalDateTime now);

    // ---------------------------------------------------------------
    // Query criteria — a null argument means "no restriction"
    // ---------------------------------------------------------------

    /** All non-null criteria must match; {@code to} includes the whole of that day. */
    static Specification<LoginHistory> matching(LoginHistorySearchCriteria criteria) {
        return Specification.allOf(
                search(criteria.search()),
                hasStatus(criteria.status()),
                hasAuthenticationMethod(criteria.authenticationMethod()),
                loggedInFrom(criteria.from() == null ? null : criteria.from().atStartOfDay()),
                loggedInBefore(criteria.to() == null ? null : criteria.to().plusDays(1).atStartOfDay()));
    }

    static Specification<LoginHistory> search(String term) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(term)) {
                return null;
            }
            String pattern = "%" + escapeLike(term.strip().toLowerCase(Locale.ROOT)) + "%";
            return cb.or(
                    cb.like(cb.lower(root.<String>get("username")), pattern, '\\'),
                    cb.like(cb.lower(root.<String>get("email")), pattern, '\\'),
                    cb.like(cb.lower(root.<String>get("employeeId")), pattern, '\\'));
        };
    }

    static Specification<LoginHistory> hasStatus(LoginStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    static Specification<LoginHistory> hasAuthenticationMethod(AuthenticationMethod method) {
        return (root, query, cb) -> method == null ? null : cb.equal(root.get("authenticationMethod"), method);
    }

    static Specification<LoginHistory> loggedInFrom(LocalDateTime from) {
        return (root, query, cb) -> from == null
                ? null
                : cb.greaterThanOrEqualTo(root.<LocalDateTime>get("loginTime"), from);
    }

    static Specification<LoginHistory> loggedInBefore(LocalDateTime before) {
        return (root, query, cb) -> before == null
                ? null
                : cb.lessThan(root.<LocalDateTime>get("loginTime"), before);
    }

    static Specification<LoginHistory> forUsername(String username) {
        return (root, query, cb) -> cb.equal(root.get("username"), username);
    }

    /** Successful logins whose session has been neither logged out nor expired. */
    static Specification<LoginHistory> activeSessionAt(LocalDateTime now) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), LoginStatus.SUCCESS),
                cb.isNull(root.get("logoutTime")),
                cb.or(
                        cb.isNull(root.get("sessionExpiresAt")),
                        cb.greaterThan(root.<LocalDateTime>get("sessionExpiresAt"), now)));
    }

    private static String escapeLike(String input) {
        return input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
