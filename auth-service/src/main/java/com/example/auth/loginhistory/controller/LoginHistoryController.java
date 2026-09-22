package com.example.auth.loginhistory.controller;

import com.example.auth.loginhistory.dto.LoginHistoryResponseDto;
import com.example.auth.loginhistory.dto.LoginHistorySearchCriteria;
import com.example.auth.loginhistory.dto.LoginHistorySummaryDto;
import com.example.auth.loginhistory.dto.LoginHoursStatusDto;
import com.example.auth.loginhistory.service.LoginHistoryService;
import com.example.common.response.ApiResponse;
import com.example.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * REST controller for the Login History screen (Story 1.6.2).
 * Read-only by design: records are written by the authentication flow, never through this API,
 * which is why it does not extend AbstractController.
 */
@RestController
@RequestMapping("/login-history")
@Tag(name = "Login History", description = "Login attempts, logouts and session status")
public class LoginHistoryController {

    private static final Logger log = LoggerFactory.getLogger(LoginHistoryController.class);

    private static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final LoginHistoryService loginHistoryService;

    public LoginHistoryController(LoginHistoryService loginHistoryService) {
        this.loginHistoryService = loginHistoryService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search login history",
            description = "Paged, newest first. 'search' matches username, email or employee ID; "
                    + "'from' and 'to' are inclusive dates (yyyy-MM-dd).")
    public ResponseEntity<ApiResponse<PageResponse<LoginHistoryResponseDto>>> search(
            @ParameterObject LoginHistorySearchCriteria criteria,
            @ParameterObject @PageableDefault(size = 20, sort = "loginTime", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(loginHistoryService.search(criteria, pageable))));
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search login history (request body)",
            description = "Same result as GET /login-history, but accepts the filters as a JSON body "
                    + "instead of query parameters — useful for larger or more complex filter sets.")
    public ResponseEntity<ApiResponse<PageResponse<LoginHistoryResponseDto>>> searchByBody(
            @RequestBody(required = false) LoginHistorySearchCriteria criteria,
            @ParameterObject @PageableDefault(size = 20, sort = "loginTime", direction = Sort.Direction.DESC)
            Pageable pageable) {

        LoginHistorySearchCriteria effective = criteria != null
                ? criteria
                : new LoginHistorySearchCriteria(null, null, null, null, null);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(loginHistoryService.search(effective, pageable))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "View login record details")
    public ResponseEntity<ApiResponse<LoginHistoryResponseDto>> getById(
            @PathVariable Long id, Authentication authentication) {

        log.info("Login history record {} viewed by '{}'", id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(loginHistoryService.getById(id)));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Login summary",
            description = "Today's logins, split into successful and failed, and the sessions active right now.")
    public ResponseEntity<ApiResponse<LoginHistorySummaryDto>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok(loginHistoryService.getSummary()));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export login history as CSV",
            description = "Exports only the records matching the given filters.")
    public ResponseEntity<byte[]> export(
            @ParameterObject LoginHistorySearchCriteria criteria, Authentication authentication) {

        byte[] csv = loginHistoryService.exportCsv(criteria);
        log.info("Login history exported by '{}' with filters {}", authentication.getName(), criteria);

        String filename = "login-history-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .contentType(TEXT_CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(csv);
    }

    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export login history as CSV (request body)",
            description = "Same export as GET /login-history/export, but accepts the filters as a JSON body.")
    public ResponseEntity<byte[]> exportByBody(
            @RequestBody(required = false) LoginHistorySearchCriteria criteria, Authentication authentication) {

        LoginHistorySearchCriteria effective = criteria != null
                ? criteria
                : new LoginHistorySearchCriteria(null, null, null, null, null);

        byte[] csv = loginHistoryService.exportCsv(effective);
        log.info("Login history exported by '{}' with filters {}", authentication.getName(), effective);

        String filename = "login-history-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .contentType(TEXT_CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(csv);
    }

    @PostMapping("/{id}/logout")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Force logout a session",
            description = "Admin action: immediately closes the session tied to this login history record, "
                    + "the same way the user's own logout would. Fails if the record has no active session.")
    public ResponseEntity<ApiResponse<LoginHistoryResponseDto>> terminateSession(
            @PathVariable Long id, Authentication authentication) {

        LoginHistoryResponseDto result = loginHistoryService.terminateSession(id);
        log.info("Login history record {} force-logged-out by '{}'", id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok("Session terminated", result));
    }

    @GetMapping("/me")
    @Operation(summary = "My login history", description = "The caller's own login attempts, newest first.")
    public ResponseEntity<ApiResponse<PageResponse<LoginHistoryResponseDto>>> getMyHistory(
            Authentication authentication,
            @ParameterObject @PageableDefault(size = 20, sort = "loginTime", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(
                loginHistoryService.getHistoryForUser(authentication.getName(), pageable))));
    }

    @GetMapping("/me/status")
    @Operation(summary = "My login hours status",
            description = "Check-in/checkout style status card for the caller's most recent session: "
                    + "login time, logout time (if any), hours worked, hours remaining against the "
                    + "required work day (9h by default, app.login-history.required-work-minutes), "
                    + "whether the session is still active, and whether it was closed manually, "
                    + "automatically (session expiry) or by an admin.")
    public ResponseEntity<ApiResponse<LoginHoursStatusDto>> getMyStatus(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(loginHistoryService.getMyLoginStatus(authentication.getName())));
    }
}
