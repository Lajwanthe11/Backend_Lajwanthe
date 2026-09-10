package com.enterprise.superadmin.feature_management_service.service;

import com.enterprise.superadmin.feature_management_service.dto.request.FeatureCreateRequest;
import com.enterprise.superadmin.feature_management_service.dto.request.FeatureUpdateRequest;
import com.enterprise.superadmin.feature_management_service.dto.response.FeatureResponse;
import com.enterprise.superadmin.feature_management_service.entity.Feature;
import com.enterprise.superadmin.feature_management_service.exception.FeatureConfigurationException;
import com.enterprise.superadmin.feature_management_service.exception.FeatureNotFoundException;
import com.enterprise.superadmin.feature_management_service.exception.InvalidFeatureStateException;
import com.enterprise.superadmin.feature_management_service.repository.FeatureRepository;
import com.enterprise.superadmin.feature_management_service.services.FeatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureServiceTest {

    @Mock
    private FeatureRepository featureRepository;

    @InjectMocks
    private FeatureService featureService;

    private UUID featureId;
    private UUID userId;
    private Feature feature;

    @BeforeEach
    void setUp() {

        featureId = UUID.randomUUID();
        userId = UUID.randomUUID();

        feature = new Feature();

        feature.setId(featureId);
        feature.setFeatureName("Test Feature");
        feature.setModule("Test Module");
        feature.setLicensePlan("PREMIUM");
        feature.setStatus("ENABLED");
        feature.setConfiguration("{\"key\":\"value\"}");
        feature.setCreatedBy(userId);
        feature.setUpdatedBy(userId);
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void createFeature_shouldCreateSuccessfully() {

        FeatureCreateRequest request = new FeatureCreateRequest();

        request.setFeatureName("Test Feature");
        request.setModule("Test Module");
        request.setLicensePlan("PREMIUM");
        request.setStatus("ENABLED");
        request.setConfiguration("{\"key\":\"value\"}");
        request.setCreatedBy(userId.toString());

        when(featureRepository.existsByFeatureName("Test Feature"))
                .thenReturn(false);

        when(featureRepository.save(any(Feature.class)))
                .thenReturn(feature);

        FeatureResponse response =
                featureService.createFeature(request);

        assertNotNull(response);
        assertEquals(featureId, response.getId());
        assertEquals("Test Feature", response.getFeatureName());
        assertEquals("ENABLED", response.getStatus());

        verify(featureRepository)
                .existsByFeatureName("Test Feature");

        verify(featureRepository)
                .save(any(Feature.class));
    }

    @Test
    void createFeature_shouldThrowExceptionWhenFeatureAlreadyExists() {

        FeatureCreateRequest request = new FeatureCreateRequest();

        request.setFeatureName("Test Feature");

        when(featureRepository.existsByFeatureName("Test Feature"))
                .thenReturn(true);

        assertThrows(
                InvalidFeatureStateException.class,
                () -> featureService.createFeature(request)
        );

        verify(featureRepository, never())
                .save(any(Feature.class));
    }

    @Test
    void createFeature_shouldRejectInvalidStatus() {

        FeatureCreateRequest request = new FeatureCreateRequest();

        request.setFeatureName("Test Feature");
        request.setStatus("INVALID");

        when(featureRepository.existsByFeatureName("Test Feature"))
                .thenReturn(false);

        assertThrows(
                InvalidFeatureStateException.class,
                () -> featureService.createFeature(request)
        );

        verify(featureRepository, never())
                .save(any(Feature.class));
    }

    @Test
    void createFeature_shouldRejectInvalidConfiguration() {

        FeatureCreateRequest request = new FeatureCreateRequest();

        request.setFeatureName("Test Feature");
        request.setStatus("ENABLED");
        request.setConfiguration("invalid-json");

        when(featureRepository.existsByFeatureName("Test Feature"))
                .thenReturn(false);

        assertThrows(
                FeatureConfigurationException.class,
                () -> featureService.createFeature(request)
        );

        verify(featureRepository, never())
                .save(any(Feature.class));
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @Test
    void getAllFeatures_shouldReturnFeatures() {

        when(featureRepository.findAll())
                .thenReturn(List.of(feature));

        List<FeatureResponse> response =
                featureService.getAllFeatures();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(
                "Test Feature",
                response.get(0).getFeatureName()
        );

        verify(featureRepository).findAll();
    }

    @Test
    void getAllFeatures_shouldReturnEmptyList() {

        when(featureRepository.findAll())
                .thenReturn(List.of());

        List<FeatureResponse> response =
                featureService.getAllFeatures();

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(featureRepository).findAll();
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Test
    void getFeatureById_shouldReturnFeature() {

        when(featureRepository.findById(featureId))
                .thenReturn(Optional.of(feature));

        FeatureResponse response =
                featureService.getFeatureById(featureId);

        assertNotNull(response);
        assertEquals(featureId, response.getId());
        assertEquals(
                "Test Feature",
                response.getFeatureName()
        );

        verify(featureRepository).findById(featureId);
    }

    @Test
    void getFeatureById_shouldThrowExceptionWhenNotFound() {

        when(featureRepository.findById(featureId))
                .thenReturn(Optional.empty());

        assertThrows(
                FeatureNotFoundException.class,
                () -> featureService.getFeatureById(featureId)
        );

        verify(featureRepository).findById(featureId);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void updateFeature_shouldUpdateSuccessfully() {

        FeatureUpdateRequest request = new FeatureUpdateRequest();

        request.setFeatureName("Updated Feature");
        request.setModule("Updated Module");
        request.setLicensePlan("ENTERPRISE");
        request.setStatus("DISABLED");
        request.setConfiguration("{\"updated\":true}");
        request.setUpdatedBy(userId.toString());

        when(featureRepository.findById(featureId))
                .thenReturn(Optional.of(feature));

        when(featureRepository.save(any(Feature.class)))
                .thenReturn(feature);

        FeatureResponse response =
                featureService.updateFeature(
                        featureId,
                        request,
                        userId
                );

        assertNotNull(response);

        assertEquals(
                "Updated Feature",
                feature.getFeatureName()
        );

        assertEquals(
                "Updated Module",
                feature.getModule()
        );

        assertEquals(
                "ENTERPRISE",
                feature.getLicensePlan()
        );

        assertEquals(
                "DISABLED",
                feature.getStatus()
        );

        assertEquals(
                "{\"updated\":true}",
                feature.getConfiguration()
        );

        assertEquals(
                userId,
                feature.getUpdatedBy()
        );

        verify(featureRepository)
                .findById(featureId);

        verify(featureRepository)
                .save(feature);
    }

    @Test
    void updateFeature_shouldThrowExceptionWhenNotFound() {

        FeatureUpdateRequest request = new FeatureUpdateRequest();

        when(featureRepository.findById(featureId))
                .thenReturn(Optional.empty());

        assertThrows(
                FeatureNotFoundException.class,
                () -> featureService.updateFeature(
                        featureId,
                        request,
                        userId
                )
        );

        verify(featureRepository, never())
                .save(any(Feature.class));
    }

    @Test
    void updateFeature_shouldRejectInvalidStatus() {

        FeatureUpdateRequest request = new FeatureUpdateRequest();

        request.setStatus("INVALID");

        when(featureRepository.findById(featureId))
                .thenReturn(Optional.of(feature));

        assertThrows(
                InvalidFeatureStateException.class,
                () -> featureService.updateFeature(
                        featureId,
                        request,
                        userId
                )
        );

        verify(featureRepository, never())
                .save(any(Feature.class));
    }

    @Test
    void updateFeature_shouldRejectInvalidConfiguration() {

        FeatureUpdateRequest request = new FeatureUpdateRequest();

        request.setConfiguration("invalid-json");

        when(featureRepository.findById(featureId))
                .thenReturn(Optional.of(feature));

        assertThrows(
                FeatureConfigurationException.class,
                () -> featureService.updateFeature(
                        featureId,
                        request,
                        userId
                )
        );

        verify(featureRepository, never())
                .save(any(Feature.class));
    }

    // =========================================================
    // ENABLE
    // =========================================================

    @Test
    void enableFeature_shouldEnableFeature() {

        feature.setStatus("DISABLED");

        when(featureRepository.findById(featureId))
                .thenReturn(Optional.of(feature));

        when(featureRepository.save(any(Feature.class)))
                .thenReturn(feature);

        FeatureResponse response =
                featureService.enableFeature(featureId);

        assertEquals("ENABLED", feature.getStatus());
        assertNotNull(response);

        verify(featureRepository).save(feature);
    }

    @Test
    void enableFeature_shouldThrowWhenAlreadyEnabled() {

        feature.setStatus("ENABLED");

        when(featureRepository.findById(featureId))
                .thenReturn(Optional.of(feature));

        assertThrows(
                InvalidFeatureStateException.class,
                () -> featureService.enableFeature(featureId)
        );

        verify(featureRepository, never())
                .save(any(Feature.class));
    }

    // =========================================================
    // DISABLE
    // =========================================================

    @Test
    void disableFeature_shouldDisableFeature() {

        feature.setStatus("ENABLED");

        when(featureRepository.findById(featureId))
                .thenReturn(Optional.of(feature));

        when(featureRepository.save(any(Feature.class)))
                .thenReturn(feature);

        FeatureResponse response =
                featureService.disableFeature(featureId);

        assertEquals("DISABLED", feature.getStatus());
        assertNotNull(response);

        verify(featureRepository).save(feature);
    }

    @Test
    void disableFeature_shouldThrowWhenAlreadyDisabled() {

        feature.setStatus("DISABLED");

        when(featureRepository.findById(featureId))
                .thenReturn(Optional.of(feature));

        assertThrows(
                InvalidFeatureStateException.class,
                () -> featureService.disableFeature(featureId)
        );

        verify(featureRepository, never())
                .save(any(Feature.class));
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void deleteFeature_shouldDeleteFeature() {

        when(featureRepository.findById(featureId))
                .thenReturn(Optional.of(feature));

        featureService.deleteFeature(featureId);

        verify(featureRepository)
                .findById(featureId);

        verify(featureRepository)
                .delete(feature);
    }

    @Test
    void deleteFeature_shouldThrowExceptionWhenNotFound() {

        when(featureRepository.findById(featureId))
                .thenReturn(Optional.empty());

        assertThrows(
                FeatureNotFoundException.class,
                () -> featureService.deleteFeature(featureId)
        );

        verify(featureRepository, never())
                .delete(any(Feature.class));
    }
}
