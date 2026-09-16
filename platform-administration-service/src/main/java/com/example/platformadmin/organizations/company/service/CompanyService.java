package com.example.platformadmin.organizations.company.service;

import com.example.common.abstracts.BaseService;
import com.example.platformadmin.organizations.company.dto.CompanyRequestDto;
import com.example.platformadmin.organizations.company.dto.CompanyResponseDto;
import com.example.platformadmin.organizations.company.entity.Company;

import java.util.List;

/**
 * Service contract for Company CRUD operations and search.
 */
public interface CompanyService
        extends BaseService<Company, Long, CompanyRequestDto, CompanyResponseDto> {

    List<CompanyResponseDto> searchCompanies(String query);
}
