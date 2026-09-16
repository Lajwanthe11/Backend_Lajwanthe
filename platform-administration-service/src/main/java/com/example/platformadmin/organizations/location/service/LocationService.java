package com.example.platformadmin.organizations.location.service;

import com.example.common.abstracts.AbstractService;
import com.example.platformadmin.organizations.location.dto.LocationRequestDto;
import com.example.platformadmin.organizations.location.dto.LocationResponseDto;
import com.example.platformadmin.organizations.location.entity.LocationEntity;
import com.example.platformadmin.organizations.location.repository.LocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService extends AbstractService<
        LocationEntity,
        Long,
        LocationRequestDto,
        LocationResponseDto> {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        super(locationRepository, "Location");
        this.locationRepository = locationRepository;
    }

    @Override
    protected LocationEntity toEntity(LocationRequestDto dto) {

        LocationEntity entity = new LocationEntity();

        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setAddressLine1(dto.getAddressLine1());
        entity.setAddressLine2(dto.getAddressLine2());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setCountry(dto.getCountry());
        entity.setPostalCode(dto.getPostalCode());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setCompanyId(dto.getCompanyId());
        entity.setBranchId(dto.getBranchId());
        entity.setStatus(dto.getStatus());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setLocationCode(dto.getLocationCode());
        entity.setLocationName(dto.getLocationName());

        entity.setActive(
                dto.getActive() != null
                        ? dto.getActive()
                        : true
        );

        return entity;
    }

    @Override
    protected LocationResponseDto toDto(LocationEntity entity) {

        LocationResponseDto dto = new LocationResponseDto();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setAddressLine1(entity.getAddressLine1());
        dto.setAddressLine2(entity.getAddressLine2());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setCountry(entity.getCountry());
        dto.setPostalCode(entity.getPostalCode());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setCompanyId(entity.getCompanyId());
        dto.setBranchId(entity.getBranchId());
        dto.setActive(entity.getActive());
        dto.setStatus(entity.getStatus());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setLocationCode(entity.getLocationCode());
        dto.setLocationName(entity.getLocationName());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    @Override
    protected void updateEntityFromDto(
            LocationEntity entity,
            LocationRequestDto dto) {

        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setAddressLine1(dto.getAddressLine1());
        entity.setAddressLine2(dto.getAddressLine2());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setCountry(dto.getCountry());
        entity.setPostalCode(dto.getPostalCode());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setCompanyId(dto.getCompanyId());
        entity.setBranchId(dto.getBranchId());
        entity.setStatus(dto.getStatus());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setLocationCode(dto.getLocationCode());
        entity.setLocationName(dto.getLocationName());

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
    }

    public Page<LocationResponseDto> search(
            String query,
            Pageable pageable) {

        return locationRepository
                .search(query, pageable)
                .map(this::toDto);
    }

    public List<LocationResponseDto> getByCompanyId(Long companyId) {

        return locationRepository
                .findByCompanyId(companyId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<LocationResponseDto> getByBranchId(Long branchId) {

        return locationRepository
                .findByBranchId(branchId)
                .stream()
                .map(this::toDto)
                .toList();
    }
}