package com.enterprise.superadmin.platform_branding_service.service;

import com.enterprise.superadmin.platform_branding_service.dto.request.BrandingCreateRequest;
import com.enterprise.superadmin.platform_branding_service.dto.request.BrandingUpdateRequest;
import com.enterprise.superadmin.platform_branding_service.dto.response.BrandingResponse;

/**
 * Service interface for managing platform branding.
 *
 * This service provides methods to create, update, reset, preview, and publish
 * platform branding configurations. It abstracts the underlying persistence
 * and business logic, providing a clean API for clients.
 */
public interface BrandingService {

    BrandingResponse getCurrentBranding();

    BrandingResponse createBranding(BrandingCreateRequest request);

    BrandingResponse updateBranding(BrandingUpdateRequest request);

    BrandingResponse resetBranding();

    BrandingResponse previewBranding(BrandingUpdateRequest request);

    BrandingResponse publishBranding();
}