package com.example.auth.securityalerts.controller;

import com.example.auth.securityalerts.dto.SecurityEventFilter;
import com.example.auth.securityalerts.dto.SecurityEventIngestResponse;
import com.example.auth.securityalerts.dto.SecurityEventRequest;
import com.example.auth.securityalerts.dto.SecurityEventResponse;
import com.example.auth.securityalerts.service.SecurityEventService;
import com.example.common.response.ApiResponse;
import com.example.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/security-alerts/events")
@Tag(name = "Security Events", description = "Security event log and inter-service event ingestion")
public class SecurityEventController {

    public static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final SecurityEventService eventService;

    public SecurityEventController(SecurityEventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @PreAuthorize(SecurityAlertController.ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Security Event Log", description = "Every event reported by the platform modules, including those that raised no alert.")
    public ResponseEntity<ApiResponse<PageResponse<SecurityEventResponse>>> searchEvents(
            @ParameterObject SecurityEventFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(eventService.searchEvents(filter, pageable))));
    }

    /** Open in SecurityConfig: the caller is another service, authenticated by the shared API key rather than a user JWT. */
    @PostMapping("/ingest")
    @Operation(summary = "Report Security Event (inter-service)",
            description = "For other microservices. Send the shared secret in the "
                    + API_KEY_HEADER + " header. Not for browsers. The response lists any alert the event raised or joined.")
    public ResponseEntity<ApiResponse<SecurityEventIngestResponse>> ingest(
            @RequestHeader(value = API_KEY_HEADER, required = false) String apiKey,
            @Valid @RequestBody SecurityEventRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Security event recorded", eventService.ingest(request, apiKey)));
    }
}
