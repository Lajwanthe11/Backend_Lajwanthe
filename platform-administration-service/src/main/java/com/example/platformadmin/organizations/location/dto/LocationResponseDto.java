package com.example.platformadmin.organizations.location.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LocationResponseDto {

    private Long id;
    private String name;
    private String code;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String phone;
    private String email;
    private Long companyId;
    private Long branchId;
    private Boolean active;
    private String status;
    private Double latitude;
    private Double longitude;
    private String locationCode;
    private String locationName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}