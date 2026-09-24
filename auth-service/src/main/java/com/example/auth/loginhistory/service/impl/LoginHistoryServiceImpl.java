package com.example.auth.loginhistory.service.impl;

import com.example.auth.loginhistory.dto.LoginHistoryResponseDto;
import com.example.auth.loginhistory.dto.LoginHistoryResponseDto.SessionStatus;
import com.example.auth.loginhistory.dto.LoginHistoryResponseDto.WorkHoursStatus;
import com.example.auth.loginhistory.dto.LoginHistorySearchCriteria;
import com.example.auth.loginhistory.dto.LoginHistorySummaryDto;
import com.example.auth.loginhistory.dto.LoginHoursStatusDto;
import com.example.auth.loginhistory.entity.LoginHistory;
import com.example.auth.loginhistory.entity.LoginHistory.AuthenticationMethod;
import com.example.auth.loginhistory.entity.LoginHistory.LoginStatus;
import com.example.auth.loginhistory.entity.LoginHistory.LogoutType;
import com.example.auth.loginhistory.exception.InvalidLoginHistoryRequestException;
import com.example.auth.loginhistory.exception.LoginHistoryException;
import com.example.auth.loginhistory.exception.LoginHistoryNotFoundException;
import com.example.auth.loginhistory.repository.LoginHistoryRepository;
import com.example.auth.loginhistory.service.LoginHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link LoginHistoryService}.
 * Login History is an audit trail, so this deliberately does not extend AbstractService: its
 * create/update/delete operations would let API callers forge or erase records.
 */
@Service
public class LoginHistoryServiceImpl implements LoginHistoryService {

    private static final Logger log = LoggerFactory.getLogger(LoginHistoryServiceImpl.class);

    private static final int MAX_SEARCH_LENGTH = 100;
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "loginTime");
    private static final String UNKNOWN = "Unknown";
    private static final String OTHER = "Other";

    private static final List<String> CSV_HEADER = List.of(
            "ID", "Username", "Email", "Employee ID", "Login Time", "Logout Time", "Status",
            "Authentication Method", "Provider", "Failure Reason", "Session Status", "Logout Type",
            "Session Duration (s)", "Work Hours Status", "Required Work Seconds",
            "IP Address", "Location", "Device Type", "Operating System", "Browser");

    private final LoginHistoryRepository loginHistoryRepository;
    private final boolean maskIpAddress;
    private final int exportMaxRows;
    private final long requiredWorkSeconds;

    public LoginHistoryServiceImpl(LoginHistoryRepository loginHistoryRepository,
                                   @Value("${app.login-history.mask-ip-address:false}") boolean maskIpAddress,
                                   @Value("${app.login-history.export-max-rows:10000}") int exportMaxRows,
                                   @Value("${app.login-history.required-work-minutes:540}") long requiredWorkMinutes) {
        this.loginHistoryRepository = loginHistoryRepository;
        this.maskIpAddress = maskIpAddress;
        this.exportMaxRows = exportMaxRows;
        // 540 minutes = 9 hours, the default "morning check-in to evening checkout" work day
        this.requiredWorkSeconds = requiredWorkMinutes * 60L;
    }

    // ---------------------------------------------------------------
    // Queries
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<LoginHistoryResponseDto> search(LoginHistorySearchCriteria criteria, Pageable pageable) {
        validate(criteria);
        try {
            return loginHistoryRepository.findAll(LoginHistoryRepository.matching(criteria), pageable)
                    .map(this::toDto);
        } catch (DataAccessException ex) {
            log.error("Failed to search login history with criteria {}", criteria, ex);
            throw LoginHistoryException.queryFailed("search", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LoginHistoryResponseDto getById(Long id) {
        try {
            return loginHistoryRepository.findById(id)
                    .map(this::toDto)
                    .orElseThrow(() -> LoginHistoryNotFoundException.forId(id));
        } catch (DataAccessException ex) {
            log.error("Failed to load login history record {}", id, ex);
            throw LoginHistoryException.queryFailed("load", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoginHistoryResponseDto> getHistoryForUser(String username, Pageable pageable) {
        try {
            return loginHistoryRepository.findAll(LoginHistoryRepository.forUsername(username), pageable)
                    .map(this::toDto);
        } catch (DataAccessException ex) {
            log.error("Failed to load login history for user '{}'", username, ex);
            throw LoginHistoryException.queryFailed("load", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LoginHistorySummaryDto getSummary() {
        try {
            LocalDateTime now = LocalDateTime.now();
            Specification<LoginHistory> today = LoginHistoryRepository.loggedInFrom(now.toLocalDate().atStartOfDay());

            long successful = loginHistoryRepository.count(today.and(LoginHistoryRepository.hasStatus(LoginStatus.SUCCESS)));
            long failed = loginHistoryRepository.count(today.and(LoginHistoryRepository.hasStatus(LoginStatus.FAILED)));
            long activeSessions = loginHistoryRepository.count(LoginHistoryRepository.activeSessionAt(now));

            return new LoginHistorySummaryDto(successful + failed, successful, failed, activeSessions);
        } catch (DataAccessException ex) {
            log.error("Failed to build login history summary", ex);
            throw LoginHistoryException.queryFailed("summarize", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LoginHoursStatusDto getMyLoginStatus(String username) {
        return getMyLoginStatus(username, LocalDateTime.now());
    }

    /** Package-private overload so tests can pin "now" instead of racing the wall clock. */
    LoginHoursStatusDto getMyLoginStatus(String username, LocalDateTime now) {
        try {
            return loginHistoryRepository.findFirstByUsernameOrderByLoginTimeDesc(username)
                    .filter(entry -> entry.getStatus() == LoginStatus.SUCCESS)
                    .map(entry -> toStatusDto(entry, now))
                    .orElseGet(() -> LoginHoursStatusDto.none(username, requiredWorkSeconds));
        } catch (DataAccessException ex) {
            log.error("Failed to load login hours status for user '{}'", username, ex);
            throw LoginHistoryException.queryFailed("load login hours status for", ex);
        }
    }

    private LoginHoursStatusDto toStatusDto(LoginHistory entry, LocalDateTime now) {
        LocalDateTime end = entry.getLogoutTime() != null ? entry.getLogoutTime() : now;
        long worked = Duration.between(entry.getLoginTime(), end).getSeconds();
        long remaining = Math.max(0, requiredWorkSeconds - worked);
        SessionStatus session = sessionStatus(entry, now);
        WorkHoursStatus workStatus = workHoursStatus(session, worked, requiredWorkSeconds);
        double percent = requiredWorkSeconds <= 0 ? 100.0
                : Math.round(Math.min(worked, requiredWorkSeconds) * 1000.0 / requiredWorkSeconds) / 10.0;

        return new LoginHoursStatusDto(
                true,
                entry.getId(),
                entry.getUsername(),
                entry.getLoginTime().toLocalDate(),
                entry.getLoginTime(),
                entry.getLogoutTime(),
                session,
                entry.getLogoutType(),
                worked,
                requiredWorkSeconds,
                remaining,
                workStatus,
                percent);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv(LoginHistorySearchCriteria criteria) {
        validate(criteria);

        Page<LoginHistory> page;
        try {
            page = loginHistoryRepository.findAll(
                    LoginHistoryRepository.matching(criteria), PageRequest.of(0, exportMaxRows, NEWEST_FIRST));
        } catch (DataAccessException ex) {
            log.error("Failed to load login history for export with criteria {}", criteria, ex);
            throw LoginHistoryException.queryFailed("export", ex);
        }

        // Refuse rather than silently truncate an audit report
        if (page.getTotalElements() > exportMaxRows) {
            throw new InvalidLoginHistoryRequestException(String.format(
                    "Export matches %d records but the limit is %d. Narrow the filters and try again.",
                    page.getTotalElements(), exportMaxRows));
        }

        try {
            return toCsv(page.map(this::toDto).getContent());
        } catch (RuntimeException ex) {
            log.error("Failed to generate CSV for login history export with criteria {}", criteria, ex);
            throw LoginHistoryException.exportFailed(ex);
        }
    }

    private static void validate(LoginHistorySearchCriteria criteria) {
        if (criteria.search() != null && criteria.search().strip().length() > MAX_SEARCH_LENGTH) {
            throw new InvalidLoginHistoryRequestException("search must not exceed " + MAX_SEARCH_LENGTH + " characters");
        }
        if (criteria.from() != null && criteria.to() != null && criteria.from().isAfter(criteria.to())) {
            throw new InvalidLoginHistoryRequestException("'from' date must not be later than 'to' date");
        }
    }

    // ---------------------------------------------------------------
    // Recording — no @Transactional here: each save commits on its own, so a failure surfaces
    // inside the try/catch below instead of at a commit the caller would see.
    // ---------------------------------------------------------------

    @Override
    public void recordSuccessfulLogin(String username, String email, AuthenticationMethod method,
                                      String provider, String sessionId, Date sessionExpiresAt) {
        LoginHistory entry = newEntry(username, method, provider, LoginStatus.SUCCESS);
        entry.setEmail(truncate(email, 255));
        entry.setSessionId(sessionId);
        entry.setSessionExpiresAt(toLocalDateTime(sessionExpiresAt));
        save(entry);
    }

    @Override
    public void recordFailedLogin(String username, AuthenticationMethod method, String provider,
                                  AuthenticationException cause) {
        LoginHistory entry = newEntry(username, method, provider, LoginStatus.FAILED);
        entry.setFailureReason(describe(cause));
        save(entry);
    }

    @Override
    public void recordLogout(String sessionId) {
        if (sessionId == null) {
            return;
        }
        try {
            loginHistoryRepository.findBySessionId(sessionId)
                    .filter(entry -> entry.getLogoutTime() == null)
                    .ifPresent(entry -> closeSession(entry, LocalDateTime.now(), LogoutType.MANUAL));
        } catch (RuntimeException ex) {
            log.error("Failed to record logout in login history", ex);
        }
    }

    @Override
    @Transactional
    public int autoCloseExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        try {
            List<LoginHistory> expired = loginHistoryRepository
                    .findByStatusAndLogoutTimeIsNullAndSessionExpiresAtBefore(LoginStatus.SUCCESS, now);

            for (LoginHistory entry : expired) {
                // Close at the moment the session actually expired, not at sweep time, so the recorded
                // duration reflects the true "evening checkout" rather than however late the sweep ran.
                closeSession(entry, entry.getSessionExpiresAt(), LogoutType.AUTO);
            }
            if (!expired.isEmpty()) {
                log.info("Auto-logout swept {} expired session(s)", expired.size());
            }
            return expired.size();
        } catch (DataAccessException ex) {
            log.error("Failed to auto-close expired login sessions", ex);
            throw LoginHistoryException.sessionOperationFailed("auto-close expired sessions", ex);
        }
    }

    private void closeSession(LoginHistory entry, LocalDateTime logoutTime, LogoutType type) {
        entry.setLogoutTime(logoutTime);
        entry.setLogoutType(type);
        loginHistoryRepository.save(entry);
    }

    @Override
    public void recordTokenRefresh(String sessionId, Date sessionExpiresAt) {
        if (sessionId == null) {
            return;
        }
        try {
            loginHistoryRepository.findBySessionId(sessionId)
                    .filter(entry -> entry.getLogoutTime() == null)
                    .ifPresent(entry -> {
                        entry.setSessionExpiresAt(toLocalDateTime(sessionExpiresAt));
                        loginHistoryRepository.save(entry);
                    });
        } catch (RuntimeException ex) {
            log.error("Failed to record token refresh in login history", ex);
        }
    }

    @Override
    @Transactional
    public LoginHistoryResponseDto terminateSession(Long id) {
        LoginHistory entry;
        try {
            entry = loginHistoryRepository.findById(id)
                    .orElseThrow(() -> LoginHistoryNotFoundException.forId(id));
        } catch (DataAccessException ex) {
            log.error("Failed to load login history record {} for session termination", id, ex);
            throw LoginHistoryException.queryFailed("load", ex);
        }

        if (entry.getStatus() != LoginStatus.SUCCESS) {
            throw new InvalidLoginHistoryRequestException("Login history record " + id + " has no session to terminate");
        }
        if (entry.getLogoutTime() != null) {
            throw new InvalidLoginHistoryRequestException("Login history record " + id + " is already logged out");
        }

        try {
            closeSession(entry, LocalDateTime.now(), LogoutType.ADMIN_FORCED);
        } catch (DataAccessException ex) {
            log.error("Failed to terminate session for login history record {}", id, ex);
            throw LoginHistoryException.sessionOperationFailed("terminate session " + id, ex);
        }
        return toDto(entry);
    }

    private LoginHistory newEntry(String username, AuthenticationMethod method, String provider, LoginStatus status) {
        ClientDetails client = ClientDetails.ofCurrentRequest();

        LoginHistory entry = new LoginHistory();
        entry.setUsername(truncate(username, 255));
        entry.setStatus(status);
        entry.setAuthenticationMethod(method);
        entry.setAuthProvider(truncate(provider, 50));
        entry.setLoginTime(LocalDateTime.now());
        entry.setIpAddress(truncate(client.ipAddress(), 45));
        entry.setLocation(client.location());
        entry.setUserAgent(truncate(client.userAgent(), 512));
        entry.setDeviceType(client.deviceType());
        entry.setOperatingSystem(client.operatingSystem());
        entry.setBrowser(client.browser());
        return entry;
    }

    private void save(LoginHistory entry) {
        try {
            loginHistoryRepository.save(entry);
        } catch (RuntimeException ex) {
            log.error("Failed to record {} login attempt in login history", entry.getStatus(), ex);
        }
    }

    private static String describe(AuthenticationException cause) {
        if (cause instanceof BadCredentialsException) {
            return "Invalid username or password";
        }
        if (cause instanceof LockedException) {
            return "Account locked";
        }
        if (cause instanceof DisabledException) {
            return "Account disabled";
        }
        if (cause instanceof AccountExpiredException) {
            return "Account expired";
        }
        if (cause instanceof CredentialsExpiredException) {
            return "Password expired";
        }
        return truncate(cause.getMessage(), 255);
    }

    // ---------------------------------------------------------------
    // Entity -> DTO mapping
    // ---------------------------------------------------------------

    private LoginHistoryResponseDto toDto(LoginHistory entry) {
        return toDto(entry, LocalDateTime.now());
    }

    LoginHistoryResponseDto toDto(LoginHistory entry, LocalDateTime now) {
        Long durationSeconds = entry.getLogoutTime() == null
                ? null
                : Duration.between(entry.getLoginTime(), entry.getLogoutTime()).getSeconds();

        SessionStatus session = sessionStatus(entry, now);
        WorkHoursStatus workStatus = entry.getStatus() != LoginStatus.SUCCESS
                ? null
                : workHoursStatus(session, workedSecondsSoFar(entry, now), requiredWorkSeconds);

        return new LoginHistoryResponseDto(
                entry.getId(),
                entry.getUsername(),
                entry.getEmail(),
                entry.getEmployeeId(),
                entry.getStatus(),
                entry.getAuthenticationMethod(),
                entry.getAuthProvider(),
                entry.getFailureReason(),
                entry.getLoginTime(),
                entry.getLogoutTime(),
                session,
                entry.getLogoutType(),
                durationSeconds,
                workStatus,
                entry.getStatus() == LoginStatus.SUCCESS ? requiredWorkSeconds : null,
                maskIpAddress ? maskIp(entry.getIpAddress()) : entry.getIpAddress(),
                entry.getLocation(),
                entry.getDeviceType(),
                entry.getOperatingSystem(),
                entry.getBrowser(),
                entry.getUserAgent()
        );
    }

    /** Null for failed attempts, which open no session. */
    private static SessionStatus sessionStatus(LoginHistory entry, LocalDateTime now) {
        if (entry.getStatus() != LoginStatus.SUCCESS) {
            return null;
        }
        if (entry.getLogoutTime() != null) {
            return SessionStatus.LOGGED_OUT;
        }
        if (entry.getSessionExpiresAt() != null && !entry.getSessionExpiresAt().isAfter(now)) {
            return SessionStatus.EXPIRED;
        }
        return SessionStatus.ACTIVE;
    }

    private static long workedSecondsSoFar(LoginHistory entry, LocalDateTime now) {
        LocalDateTime end = entry.getLogoutTime() != null ? entry.getLogoutTime() : now;
        return Duration.between(entry.getLoginTime(), end).getSeconds();
    }

    /**
     * Compares time worked against the required work-day length.
     * Open session (ACTIVE/EXPIRED, not yet logged out): IN_PROGRESS or COMPLETED.
     * Closed session (LOGGED_OUT): SHORTFALL or OVERTIME.
     */
    private static WorkHoursStatus workHoursStatus(SessionStatus session, long workedSeconds, long requiredSeconds) {
        if (session == null) {
            return null;
        }
        boolean metTarget = workedSeconds >= requiredSeconds;
        if (session == SessionStatus.LOGGED_OUT) {
            return metTarget ? WorkHoursStatus.OVERTIME : WorkHoursStatus.SHORTFALL;
        }
        return metTarget ? WorkHoursStatus.COMPLETED : WorkHoursStatus.IN_PROGRESS;
    }

    /** Replaces the last IPv4 octet or IPv6 group with "*". */
    static String maskIp(String ip) {
        if (ip == null) {
            return null;
        }
        int cut = ip.contains(":") ? ip.lastIndexOf(':') : ip.lastIndexOf('.');
        return cut < 0 ? ip : ip.substring(0, cut + 1) + "*";
    }

    // ---------------------------------------------------------------
    // CSV export
    // ---------------------------------------------------------------

    static byte[] toCsv(List<LoginHistoryResponseDto> records) {
        StringBuilder csv = new StringBuilder(csvRow(CSV_HEADER));
        for (LoginHistoryResponseDto record : records) {
            csv.append(csvRow(Arrays.asList(
                    text(record.id()),
                    record.username(),
                    record.email(),
                    record.employeeId(),
                    text(record.loginTime()),
                    text(record.logoutTime()),
                    text(record.status()),
                    text(record.authenticationMethod()),
                    record.authProvider(),
                    record.failureReason(),
                    text(record.sessionStatus()),
                    text(record.logoutType()),
                    text(record.sessionDurationSeconds()),
                    text(record.workHoursStatus()),
                    text(record.requiredWorkSeconds()),
                    record.ipAddress(),
                    record.location(),
                    record.deviceType(),
                    record.operatingSystem(),
                    record.browser())));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String csvRow(List<String> cells) {
        return cells.stream().map(LoginHistoryServiceImpl::escapeCsv).collect(Collectors.joining(",")) + "\r\n";
    }

    /**
     * RFC 4180 quoting, plus a leading apostrophe on any cell a spreadsheet would run as a
     * formula — the username on a failed attempt is whatever the caller typed.
     */
    static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String cell = !value.isEmpty() && "=+-@\t\r".indexOf(value.charAt(0)) >= 0 ? "'" + value : value;
        if (cell.contains(",") || cell.contains("\"") || cell.contains("\n") || cell.contains("\r")) {
            cell = "\"" + cell.replace("\"", "\"\"") + "\"";
        }
        return cell;
    }

    // ---------------------------------------------------------------
    // Client details: IP address, location and device of the caller
    // ---------------------------------------------------------------

    /**
     * Network and device details of the client behind a request. The IP address is the servlet
     * remote address; behind a proxy, set {@code server.forward-headers-strategy=native} so it
     * reflects X-Forwarded-For from trusted proxies only — reading that header directly would let
     * any client choose the recorded IP.
     */
    record ClientDetails(String ipAddress, String location, String userAgent,
                         String deviceType, String operatingSystem, String browser) {

        private static final ClientDetails NONE = new ClientDetails(null, null, null, null, null, null);

        /** Details of the request bound to the current thread, or all nulls outside a request. */
        static ClientDetails ofCurrentRequest() {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
                return of(attributes.getRequest());
            }
            return NONE;
        }

        static ClientDetails of(HttpServletRequest request) {
            String ip = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            if (userAgent == null || userAgent.isBlank()) {
                return new ClientDetails(ip, locate(ip), userAgent, UNKNOWN, UNKNOWN, UNKNOWN);
            }
            String os = detectOperatingSystem(userAgent);
            return new ClientDetails(ip, locate(ip), userAgent,
                    detectDeviceType(userAgent, os), os, detectBrowser(userAgent));
        }
    }

    /**
     * Names loopback and private-network addresses. Public addresses return null: turning them
     * into a city needs a GeoIP database, which this service does not have yet.
     */
    static String locate(String ip) {
        // Parse IP literals only — InetAddress.getByName would do a DNS lookup on a hostname
        if (ip == null || !(ip.contains(":") || ip.matches("\\d{1,3}(\\.\\d{1,3}){3}"))) {
            return null;
        }
        try {
            InetAddress address = InetAddress.getByName(ip);
            if (address.isLoopbackAddress()) {
                return "Localhost";
            }
            if (address.isSiteLocalAddress() || address.isLinkLocalAddress()) {
                return "Private network";
            }
        } catch (UnknownHostException ex) {
            // Malformed literal: leave the location unknown
        }
        return null;
    }

    // Order matters: iOS user agents also say "Mac OS X", and Android ones also say "Linux".
    private static String detectOperatingSystem(String ua) {
        if (ua.contains("Windows")) {
            return "Windows";
        }
        if (ua.contains("iPhone") || ua.contains("iPad") || ua.contains("iPod")) {
            return "iOS";
        }
        if (ua.contains("Android")) {
            return "Android";
        }
        if (ua.contains("CrOS")) {
            return "ChromeOS";
        }
        if (ua.contains("Mac OS X") || ua.contains("Macintosh")) {
            return "macOS";
        }
        if (ua.contains("Linux")) {
            return "Linux";
        }
        return OTHER;
    }

    // Order matters: Edge and Opera also say "Chrome/", and Chrome also says "Safari/".
    private static String detectBrowser(String ua) {
        if (ua.contains("Edg/") || ua.contains("EdgA/") || ua.contains("EdgiOS/")) {
            return "Edge";
        }
        if (ua.contains("OPR/") || ua.contains("Opera")) {
            return "Opera";
        }
        if (ua.contains("SamsungBrowser/")) {
            return "Samsung Internet";
        }
        if (ua.contains("Firefox/") || ua.contains("FxiOS/")) {
            return "Firefox";
        }
        if (ua.contains("Chrome/") || ua.contains("CriOS/")) {
            return "Chrome";
        }
        if (ua.contains("Safari/")) {
            return "Safari";
        }
        if (ua.contains("PostmanRuntime/")) {
            return "Postman";
        }
        if (ua.startsWith("curl/")) {
            return "curl";
        }
        return OTHER;
    }

    // Android tablets omit the "Mobile" token that Android phones send.
    private static String detectDeviceType(String ua, String os) {
        if (ua.contains("iPad") || ua.contains("Tablet") || (os.equals("Android") && !ua.contains("Mobile"))) {
            return "Tablet";
        }
        if (ua.contains("Mobi") || ua.contains("iPhone") || ua.contains("iPod")) {
            return "Mobile";
        }
        return os.equals(OTHER) ? OTHER : "Desktop";
    }

    // ---------------------------------------------------------------
    // Utilities
    // ---------------------------------------------------------------

    private static LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private static String truncate(String value, int maxLength) {
        return value == null || value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static String text(Object value) {
        return value == null ? null : value.toString();
    }
}
