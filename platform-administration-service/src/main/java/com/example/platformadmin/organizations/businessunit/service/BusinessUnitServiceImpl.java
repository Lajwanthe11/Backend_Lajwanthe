package com.example.platformadmin.organizations.businessunit.service;

import com.example.common.exception.BadRequestException;
import com.example.common.exception.ResourceNotFoundException;
import com.example.common.tenant.TenantContext;
import com.example.platformadmin.organizations.businessunit.dto.BusinessUnitRequestDto;
import com.example.platformadmin.organizations.businessunit.dto.BusinessUnitResponseDto;
import com.example.platformadmin.organizations.businessunit.entity.BusinessUnit;
import com.example.platformadmin.organizations.businessunit.repository.BusinessUnitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link BusinessUnitService}.
 * Handles soft-delete, tenant isolation, and audit fields manually
 * (BusinessUnit has its own lifecycle management rather than using AbstractService).
 */
@Service
public class BusinessUnitServiceImpl implements BusinessUnitService {

    private final BusinessUnitRepository repository;

    public BusinessUnitServiceImpl(BusinessUnitRepository repository) {
        this.repository = repository;
    }

    private String resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            }
            if (auth.getName() != null && !auth.getName().equalsIgnoreCase("anonymousUser")) {
                return auth.getName();
            }
        }
        return "SYSTEM";
    }

    @Override
    @Transactional
    public BusinessUnitResponseDto create(BusinessUnitRequestDto request) {
        if (repository.existsByUnitCodeAndIsDeletedFalse(request.getUnitCode())) {
            throw new BadRequestException("Business Unit code already exists: " + request.getUnitCode());
        }

        String currentUser = resolveCurrentUser();
        String currentTenant = TenantContext.getTenantId();
        if (currentTenant == null || currentTenant.isBlank()) {
            currentTenant = "default";
        }

        BusinessUnit entity = BusinessUnit.builder()
                .unitName(request.getUnitName())
                .unitCode(request.getUnitCode())
                .description(request.getDescription())
                .organizationId(request.getOrganizationId())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .createdAt(LocalDateTime.now())
                .createdBy(currentUser)
                .updatedAt(LocalDateTime.now())
                .updatedBy(currentUser)
                .tenantId(currentTenant)
                .isDeleted(false)
                .build();

        return mapToDto(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessUnitResponseDto getById(Long id) {
        return mapToDto(repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessUnit", "id", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessUnitResponseDto> getAll() {
        return repository.findByIsDeletedFalse().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BusinessUnitResponseDto> getAll(Pageable pageable) {
        return repository.findByIsDeletedFalse(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional
    public BusinessUnitResponseDto update(Long id, BusinessUnitRequestDto request) {
        BusinessUnit entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessUnit", "id", id));

        entity.setUnitName(request.getUnitName());
        entity.setDescription(request.getDescription());
        entity.setStatus(request.getStatus());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(resolveCurrentUser());

        return mapToDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        BusinessUnit entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessUnit", "id", id));

        entity.setIsDeleted(true);
        entity.setDeletedAt(LocalDate.now());
        entity.setDeletedBy(resolveCurrentUser());
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return repository.findByIdAndIsDeletedFalse(id).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessUnitResponseDto> searchBusinessUnits(String query) {
        return repository.searchBusinessUnits(query).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private BusinessUnitResponseDto mapToDto(BusinessUnit unit) {
        return BusinessUnitResponseDto.builder()
                .id(unit.getId())
                .unitName(unit.getUnitName())
                .unitCode(unit.getUnitCode())
                .description(unit.getDescription())
                .organizationId(unit.getOrganizationId())
                .status(unit.getStatus())
                .createdAt(unit.getCreatedAt())
                .createdBy(unit.getCreatedBy())
                .updatedAt(unit.getUpdatedAt())
                .updatedBy(unit.getUpdatedBy())
                .build();
    }
}
