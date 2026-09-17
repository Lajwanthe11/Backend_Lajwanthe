package com.example.microservice.common.abstracts;

import com.example.microservice.common.response.ApiResponse;
import com.example.microservice.common.response.PageResponse;
import com.example.microservice.common.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Generic Abstract Controller exposing standard CRUD REST API endpoints with Multi-Tenancy support.
 * Feature controllers can simply extend this class.
 *
 * @param <T> Entity type
 * @param <ID> Primary key ID type
 * @param <REQ_DTO> Request DTO type
 * @param <RES_DTO> Response DTO type
 */
public abstract class AbstractController<T, ID, REQ_DTO, RES_DTO> {

    protected final BaseService<T, ID, REQ_DTO, RES_DTO> service;

    protected AbstractController(BaseService<T, ID, REQ_DTO, RES_DTO> service) {
        this.service = service;
    }

    /**
     * Validate ID parameter.
     */
    protected void validateId(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid ID: ID cannot be null");
        }
        if (id instanceof Number number && number.longValue() <= 0) {
            throw new IllegalArgumentException("Invalid ID: ID must be positive");
        }
    }

    /**
     * Get the username of currently authenticated user from SecurityContext.
     */
    protected String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }

    /**
     * Get the active tenant identifier from TenantContext.
     */
    protected String getCurrentTenantId() {
        return TenantContext.getTenantId();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RES_DTO>> create(@Valid @RequestBody REQ_DTO requestDto) {
        RES_DTO created = service.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Resource created successfully", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RES_DTO>> getById(@PathVariable ID id) {
        validateId(id);
        RES_DTO item = service.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(item));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RES_DTO>>> getAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<RES_DTO> page = service.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RES_DTO>>> getAllUnpaged() {
        List<RES_DTO> list = service.getAll();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RES_DTO>> update(
            @PathVariable ID id,
            @Valid @RequestBody REQ_DTO requestDto) {
        validateId(id);
        RES_DTO updated = service.update(id, requestDto);
        return ResponseEntity.ok(ApiResponse.ok("Resource updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable ID id) {
        validateId(id);
        service.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Resource deleted successfully", null));
    }
}
