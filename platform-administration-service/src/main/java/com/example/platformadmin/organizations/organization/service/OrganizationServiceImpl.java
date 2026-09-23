package com.example.platformadmin.organizations.organization.service;

import com.example.common.abstracts.AbstractService;
import com.example.platformadmin.organizations.organization.dto.OrganizationRequestDTO;
import com.example.platformadmin.organizations.organization.dto.OrganizationResponseDTO;
import com.example.platformadmin.organizations.organization.entity.OrganizationEntity;
import com.example.platformadmin.organizations.organization.enums.OrganizationType;
import com.example.platformadmin.organizations.organization.exception.OrganizationAlreadyExistsException;
import com.example.platformadmin.organizations.organization.exception.OrganizationNotFoundException;
import com.example.platformadmin.organizations.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OrganizationServiceImpl extends AbstractService<
        OrganizationEntity,
        UUID,
        OrganizationRequestDTO,
        OrganizationResponseDTO> implements OrganizationService {

    private final OrganizationRepository organizationRepository;

    // Allowed countries strictly matching the One Enterprise UI dropdown
    private static final Set<String> ALLOWED_COUNTRIES = Set.of(
            "India",
            "United States",
            "United Kingdom",
            "Singapore",
            "United Arab Emirates",
            "Canada",
            "Australia",
            "Germany"
    );

    // Allowed organization statuses
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "ACTIVE",
            "INACTIVE",
            "SUSPENDED"
    );

    public OrganizationServiceImpl(OrganizationRepository organizationRepository) {
        super(organizationRepository, "Organization");
        this.organizationRepository = organizationRepository;
    }

    @Override
    protected void beforeCreate(OrganizationEntity entity, OrganizationRequestDTO dto) {
        // 1. Check duplicate code
        if (organizationRepository.existsByOrganizationCodeIgnoreCase(dto.getOrganizationCode())) {
            throw new OrganizationAlreadyExistsException(
                    String.format("Organization with code '%s' already exists", dto.getOrganizationCode()));
        }

        // 2. Validate Country
        validateCountry(dto.getCountry());
        dto.setCountry(normalizeCountry(dto.getCountry()));

        // 3. Validate Timezone
        validateTimeZone(dto.getTimeZone());

        // 4. Validate Status
        validateStatus(dto.getStatus());
    }

    @Override
    protected void beforeUpdate(OrganizationEntity entity, OrganizationRequestDTO dto) {
        // 1. Check duplicate code for another organization
        if (organizationRepository.existsByOrganizationCodeIgnoreCaseAndIdNot(dto.getOrganizationCode(), entity.getId())) {
            throw new OrganizationAlreadyExistsException(
                    String.format("Another organization with code '%s' already exists", dto.getOrganizationCode()));
        }

        // 2. Validate Country
        validateCountry(dto.getCountry());
        dto.setCountry(normalizeCountry(dto.getCountry()));

        // 3. Validate Timezone
        validateTimeZone(dto.getTimeZone());

        // 4. Validate Status
        validateStatus(dto.getStatus());
    }

    private void validateCountry(String country) {
        if (country == null || country.isBlank() || country.trim().equalsIgnoreCase("Country / Region")) {
            throw new IllegalArgumentException("Country is required. Please select a valid country.");
        }

        boolean isValid = ALLOWED_COUNTRIES.stream()
                .anyMatch(c -> c.equalsIgnoreCase(country.trim()));

        if (!isValid) {
            throw new IllegalArgumentException(
                    String.format("Invalid country '%s'. Allowed countries are: %s",
                            country, String.join(", ", ALLOWED_COUNTRIES)));
        }
    }

    private String normalizeCountry(String country) {
        return ALLOWED_COUNTRIES.stream()
                .filter(c -> c.equalsIgnoreCase(country.trim()))
                .findFirst()
                .orElse(country.trim());
    }

    private void validateTimeZone(String timeZone) {
        if (timeZone == null || timeZone.isBlank()) {
            throw new IllegalArgumentException("Time zone cannot be blank");
        }

        // Requires explicit offset like "UTC-8 (PST)" or "UTC+5:30". Plain "UTC" is rejected.
        Pattern pattern = Pattern.compile("^(?:UTC|GMT)([+-])(\\d{1,2})(?::?(\\d{2}))?.*$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(timeZone.trim());

        if (matcher.matches()) {
            String sign = matcher.group(1);
            int hours = Integer.parseInt(matcher.group(2));
            int totalHours = sign.equals("-") ? -hours : hours;

            if (totalHours >= -12 && totalHours <= 14) {
                return; // Valid offset!
            }
        }

        throw new IllegalArgumentException(
                String.format("Invalid time zone '%s'. Must include a valid UTC offset such as 'UTC-8 (PST)' or 'UTC+5:30' (between UTC-12 and UTC+14).", timeZone));
    }

    private void validateStatus(String status) {
        if (status != null && !status.isBlank() && !ALLOWED_STATUSES.contains(status.trim().toUpperCase())) {
            throw new IllegalArgumentException(
                    String.format("Invalid status '%s'. Allowed statuses are: %s",
                            status, String.join(", ", ALLOWED_STATUSES)));
        }
    }

    @Override
    protected OrganizationEntity toEntity(OrganizationRequestDTO dto) {
        return OrganizationEntity.builder()
                .organizationName(dto.getOrganizationName())
                .organizationCode(dto.getOrganizationCode())
                .organizationType(dto.getOrganizationType())
                .industry(dto.getIndustry())
                .companySize(dto.getCompanySize())
                .country(dto.getCountry())
                .state(dto.getState())
                .city(dto.getCity())
                .timeZone(dto.getTimeZone())
                .logoUrl(dto.getLogoUrl())
                .status(dto.getStatus() != null ? dto.getStatus().trim().toUpperCase() : "ACTIVE")
                .build();
    }

    @Override
    protected OrganizationResponseDTO toDto(OrganizationEntity entity) {
        if (entity == null) {
            return null;
        }

        return OrganizationResponseDTO.builder()
                .id(entity.getId())
                .organizationName(entity.getOrganizationName())
                .organizationCode(entity.getOrganizationCode())
                .organizationType(entity.getOrganizationType())
                .organizationTypeDisplayName(
                        entity.getOrganizationType() != null ? entity.getOrganizationType().getDisplayName() : null)
                .industry(entity.getIndustry())
                .companySize(entity.getCompanySize())
                .country(entity.getCountry())
                .state(entity.getState())
                .city(entity.getCity())
                .timeZone(entity.getTimeZone())
                .logoUrl(entity.getLogoUrl())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    protected void updateEntityFromDto(OrganizationEntity entity, OrganizationRequestDTO dto) {
        entity.setOrganizationName(dto.getOrganizationName());
        entity.setOrganizationCode(dto.getOrganizationCode());
        entity.setOrganizationType(dto.getOrganizationType());
        entity.setIndustry(dto.getIndustry());
        entity.setCompanySize(dto.getCompanySize());
        entity.setCountry(dto.getCountry());
        entity.setState(dto.getState());
        entity.setCity(dto.getCity());
        entity.setTimeZone(dto.getTimeZone());
        entity.setLogoUrl(dto.getLogoUrl());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            entity.setStatus(dto.getStatus().trim().toUpperCase());
        }
        entity.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponseDTO getByCode(String organizationCode) {
        return organizationRepository.findByOrganizationCode(organizationCode)
                .map(this::toDto)
                .orElseThrow(() -> new OrganizationNotFoundException(
                        String.format("Organization not found with code: '%s'", organizationCode)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponseDTO> searchOrganizations(String query) {
        if (query == null || query.trim().length() < 2) {
            throw new IllegalArgumentException("Search query must be at least 2 characters long.");
        }

        List<OrganizationEntity> entities = organizationRepository.searchOrganizations(query.trim());

        if (entities.isEmpty()) {
            throw new OrganizationNotFoundException(
                    String.format("No organizations found matching search query: '%s'", query));
        }

        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponseDTO> getByType(OrganizationType organizationType) {
        List<OrganizationEntity> entities = organizationRepository.findByOrganizationType(organizationType);

        if (entities.isEmpty()) {
            throw new OrganizationNotFoundException(
                    String.format("No organizations found for organization type: '%s'", organizationType));
        }

        return entities.stream()
                .map(this::toDto)
                .toList();
    }
}