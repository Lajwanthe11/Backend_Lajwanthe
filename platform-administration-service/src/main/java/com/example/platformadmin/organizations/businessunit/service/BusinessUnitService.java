package com.example.platformadmin.organizations.businessunit.service;

import com.example.common.abstracts.BaseService;
import com.example.platformadmin.organizations.businessunit.dto.BusinessUnitRequestDto;
import com.example.platformadmin.organizations.businessunit.dto.BusinessUnitResponseDto;
import com.example.platformadmin.organizations.businessunit.entity.BusinessUnit;

import java.util.List;

/**
 * Service contract for BusinessUnit operations.
 */
public interface BusinessUnitService
        extends BaseService<BusinessUnit, Long, BusinessUnitRequestDto, BusinessUnitResponseDto> {

    List<BusinessUnitResponseDto> searchBusinessUnits(String query);
}
