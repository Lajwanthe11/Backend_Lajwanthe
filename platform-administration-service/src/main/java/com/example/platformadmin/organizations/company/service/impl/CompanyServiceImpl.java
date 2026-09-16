package com.example.platformadmin.organizations.company.service.impl;

import com.example.common.abstracts.AbstractService;
import com.example.platformadmin.organizations.company.dto.CompanyRequestDto;
import com.example.platformadmin.organizations.company.dto.CompanyResponseDto;
import com.example.platformadmin.organizations.company.entity.Company;
import com.example.platformadmin.organizations.company.exception.CompanyConflictException;
import com.example.platformadmin.organizations.company.mapper.CompanyMapper;
import com.example.platformadmin.organizations.company.repository.CompanyRepository;
import com.example.platformadmin.organizations.company.service.CompanyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Implementation of {@link CompanyService} using {@link AbstractService} for common CRUD.
 */
@Service
public class CompanyServiceImpl
        extends AbstractService<Company, Long, CompanyRequestDto, CompanyResponseDto>
        implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper mapper;

    public CompanyServiceImpl(CompanyRepository companyRepository, CompanyMapper mapper) {
        super(companyRepository, "Company");
        this.companyRepository = companyRepository;
        this.mapper = mapper;
    }

    @Override
    protected Company toEntity(CompanyRequestDto request) {
        return mapper.toEntity(request);
    }

    @Override
    protected CompanyResponseDto toDto(Company company) {
        return mapper.toResponse(company);
    }

    @Override
    protected void updateEntityFromDto(Company company, CompanyRequestDto request) {
        mapper.updateEntity(request, company);
    }

    @Override
    protected void beforeCreate(Company company, CompanyRequestDto request) {
        ensureCodeAvailable(request.companyCode(), null);
        company.setDeleted(false);
    }

    @Override
    protected void beforeUpdate(Company company, CompanyRequestDto request) {
        ensureCodeAvailable(request.companyCode(), company.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponseDto> searchCompanies(String query) {
        String cleaned = query == null ? null : query.strip();
        if (cleaned == null || cleaned.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        if (cleaned.length() > 100) {
            throw new IllegalArgumentException("query must not exceed 100 characters");
        }
        String pattern = "%" + escapeLike(cleaned.toLowerCase(Locale.ROOT)) + "%";
        return companyRepository.searchCompanies(pattern).stream()
                .map(mapper::toResponse)
                .toList();
    }

    private void ensureCodeAvailable(String companyCode, Long currentId) {
        boolean exists = currentId == null
                ? companyRepository.existsByCompanyCodeIgnoreCase(companyCode)
                : companyRepository.existsByCompanyCodeIgnoreCaseAndIdNot(companyCode, currentId);
        if (exists) {
            throw new CompanyConflictException("Company code already exists: " + companyCode);
        }
    }

    private static String escapeLike(String input) {
        return input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
