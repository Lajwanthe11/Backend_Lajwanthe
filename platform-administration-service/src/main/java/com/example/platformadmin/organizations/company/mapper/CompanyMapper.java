package com.example.platformadmin.organizations.company.mapper;

import com.example.platformadmin.organizations.company.dto.CompanyRequestDto;
import com.example.platformadmin.organizations.company.dto.CompanyResponseDto;
import com.example.platformadmin.organizations.company.entity.Company;
import org.springframework.stereotype.Component;

/**
 * Stateless mapper between Company entity and its DTOs.
 */
@Component
public class CompanyMapper {

    public Company toEntity(CompanyRequestDto request) {
        Company company = new Company();
        updateEntity(request, company);
        return company;
    }

    public void updateEntity(CompanyRequestDto request, Company company) {
        company.setCompanyCode(request.companyCode());
        company.setCompanyName(request.companyName());
        company.setEmail(request.email());
        company.setIndustry(request.industry());
        company.setCountry(request.country());
        company.setStatus(request.status());
    }

    public CompanyResponseDto toResponse(Company company) {
        return new CompanyResponseDto(
                company.getId(),
                company.getCompanyCode(),
                company.getCompanyName(),
                company.getEmail(),
                company.getIndustry(),
                company.getCountry(),
                company.getStatus(),
                company.getCreatedAt(),
                company.getCreatedBy(),
                company.getUpdatedAt(),
                company.getUpdatedBy()
        );
    }
}
