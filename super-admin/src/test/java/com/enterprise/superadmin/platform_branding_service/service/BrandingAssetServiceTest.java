package com.enterprise.superadmin.platform_branding_service.service;

import com.enterprise.superadmin.platform_branding_service.integration.storage.StorageClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandingAssetServiceTest {

    @Mock
    private StorageClient storageClient;

    private BrandingAssetService brandingAssetService;

    @BeforeEach
    void setUp() {
        brandingAssetService =
                new BrandingAssetService(storageClient);
    }

    // ============================================================
    // LOGO TESTS
    // ============================================================

    @Test
    void uploadLogo_shouldUploadSuccessfully() throws Exception {

        MultipartFile file = createFile(
                "logo.png",
                "image/png",
                1024
        );

        when(storageClient.upload(
                file,
                "branding/logo"
        )).thenReturn("branding/logo/logo.png");

        String result =
                brandingAssetService.uploadLogo(file);

        assertEquals(
                "branding/logo/logo.png",
                result
        );

        verify(storageClient)
                .upload(
                        file,
                        "branding/logo"
                );
    }

    @Test
    void uploadLogo_shouldRejectEmptyFile() {

        MultipartFile file =
                new MockMultipartFile(
                        "file",
                        "logo.png",
                        "image/png",
                        new byte[0]
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> brandingAssetService.uploadLogo(file)
        );

        verifyNoInteractions(storageClient);
    }

    @Test
    void uploadLogo_shouldRejectUnsupportedExtension() {

        MultipartFile file = createFile(
                "logo.gif",
                "image/png",
                1024
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> brandingAssetService.uploadLogo(file)
        );

        verifyNoInteractions(storageClient);
    }

    @Test
    void uploadLogo_shouldRejectUnsupportedMimeType() {

        MultipartFile file = createFile(
                "logo.png",
                "image/gif",
                1024
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> brandingAssetService.uploadLogo(file)
        );

        verifyNoInteractions(storageClient);
    }

    @Test
    void uploadLogo_shouldRejectFileLargerThan5MB() {

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty())
                .thenReturn(false);

        when(file.getOriginalFilename())
                .thenReturn("logo.png");

        when(file.getContentType())
                .thenReturn("image/png");

        when(file.getSize())
                .thenReturn(5L * 1024 * 1024 + 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> brandingAssetService.uploadLogo(file)
        );

        verifyNoInteractions(storageClient);
    }

    @Test
    void replaceLogo_shouldReplaceSuccessfully() throws Exception {

        MultipartFile file = createFile(
                "new-logo.png",
                "image/png",
                1024
        );

        when(storageClient.replace(
                "old-logo-reference",
                file,
                "branding/logo"
        )).thenReturn("new-logo-reference");

        String result =
                brandingAssetService.replaceLogo(
                        "old-logo-reference",
                        file
                );

        assertEquals(
                "new-logo-reference",
                result
        );

        verify(storageClient)
                .replace(
                        "old-logo-reference",
                        file,
                        "branding/logo"
                );
    }

    @Test
    void replaceLogo_shouldUploadWhenExistingReferenceIsMissing()
            throws Exception {

        MultipartFile file = createFile(
                "logo.png",
                "image/png",
                1024
        );

        when(storageClient.upload(
                file,
                "branding/logo"
        )).thenReturn("new-logo-reference");

        String result =
                brandingAssetService.replaceLogo(
                        null,
                        file
                );

        assertEquals(
                "new-logo-reference",
                result
        );

        verify(storageClient)
                .upload(
                        file,
                        "branding/logo"
                );

        verify(storageClient, never())
                .replace(
                        anyString(),
                        any(MultipartFile.class),
                        anyString()
                );
    }

    @Test
    void deleteLogo_shouldDeleteSuccessfully() throws Exception {

        brandingAssetService.deleteLogo(
                "logo-reference"
        );

        verify(storageClient)
                .delete("logo-reference");
    }

    @Test
    void deleteLogo_shouldIgnoreEmptyReference() throws Exception {

        brandingAssetService.deleteLogo(null);

        verifyNoInteractions(storageClient);
    }

    // ============================================================
    // LOGIN BACKGROUND TESTS
    // ============================================================

    @Test
    void uploadLoginBackground_shouldUploadSuccessfully()
            throws Exception {

        MultipartFile file = createFile(
                "background.jpg",
                "image/jpeg",
                2048
        );

        when(storageClient.upload(
                file,
                "branding/login-background"
        )).thenReturn("background-reference");

        String result =
                brandingAssetService.uploadLoginBackground(file);

        assertEquals(
                "background-reference",
                result
        );

        verify(storageClient)
                .upload(
                        file,
                        "branding/login-background"
                );
    }

    @Test
    void uploadLoginBackground_shouldRejectUnsupportedExtension() {

        MultipartFile file = createFile(
                "background.svg",
                "image/svg+xml",
                1024
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> brandingAssetService
                        .uploadLoginBackground(file)
        );

        verifyNoInteractions(storageClient);
    }

    @Test
    void uploadLoginBackground_shouldRejectFileLargerThan10MB() {

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty())
                .thenReturn(false);

        when(file.getOriginalFilename())
                .thenReturn("background.png");

        when(file.getContentType())
                .thenReturn("image/png");

        when(file.getSize())
                .thenReturn(10L * 1024 * 1024 + 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> brandingAssetService
                        .uploadLoginBackground(file)
        );

        verifyNoInteractions(storageClient);
    }

    @Test
    void replaceLoginBackground_shouldReplaceSuccessfully()
            throws Exception {

        MultipartFile file = createFile(
                "background.png",
                "image/png",
                2048
        );

        when(storageClient.replace(
                "old-background",
                file,
                "branding/login-background"
        )).thenReturn("new-background");

        String result =
                brandingAssetService.replaceLoginBackground(
                        "old-background",
                        file
                );

        assertEquals(
                "new-background",
                result
        );

        verify(storageClient)
                .replace(
                        "old-background",
                        file,
                        "branding/login-background"
                );
    }

    @Test
    void deleteLoginBackground_shouldDeleteSuccessfully()
            throws Exception {

        brandingAssetService.deleteLoginBackground(
                "background-reference"
        );

        verify(storageClient)
                .delete("background-reference");
    }

    // ============================================================
    // FAVICON TESTS
    // ============================================================

    @Test
    void uploadFavicon_shouldUploadSuccessfully()
            throws Exception {

        MultipartFile file = createFile(
                "favicon.ico",
                "image/x-icon",
                1024
        );

        when(storageClient.upload(
                file,
                "branding/favicon"
        )).thenReturn("favicon-reference");

        String result =
                brandingAssetService.uploadFavicon(file);

        assertEquals(
                "favicon-reference",
                result
        );

        verify(storageClient)
                .upload(
                        file,
                        "branding/favicon"
                );
    }

    @Test
    void uploadFavicon_shouldRejectUnsupportedExtension() {

        MultipartFile file = createFile(
                "favicon.gif",
                "image/gif",
                1024
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> brandingAssetService.uploadFavicon(file)
        );

        verifyNoInteractions(storageClient);
    }

    @Test
    void replaceFavicon_shouldReplaceSuccessfully()
            throws Exception {

        MultipartFile file = createFile(
                "favicon.png",
                "image/png",
                1024
        );

        when(storageClient.replace(
                "old-favicon",
                file,
                "branding/favicon"
        )).thenReturn("new-favicon");

        String result =
                brandingAssetService.replaceFavicon(
                        "old-favicon",
                        file
                );

        assertEquals(
                "new-favicon",
                result
        );

        verify(storageClient)
                .replace(
                        "old-favicon",
                        file,
                        "branding/favicon"
                );
    }

    @Test
    void replaceFavicon_shouldUploadWhenExistingReferenceIsMissing()
            throws Exception {

        MultipartFile file = createFile(
                "favicon.ico",
                "image/x-icon",
                1024
        );

        when(storageClient.upload(
                file,
                "branding/favicon"
        )).thenReturn("favicon-reference");

        String result =
                brandingAssetService.replaceFavicon(
                        "",
                        file
                );

        assertEquals(
                "favicon-reference",
                result
        );

        verify(storageClient)
                .upload(
                        file,
                        "branding/favicon"
                );

        verify(storageClient, never())
                .replace(
                        anyString(),
                        any(MultipartFile.class),
                        anyString()
                );
    }

    @Test
    void deleteFavicon_shouldDeleteSuccessfully()
            throws Exception {

        brandingAssetService.deleteFavicon(
                "favicon-reference"
        );

        verify(storageClient)
                .delete("favicon-reference");
    }

    // ============================================================
    // EMAIL HEADER LOGO TESTS
    // ============================================================

    @Test
    void uploadEmailHeaderLogo_shouldUploadSuccessfully()
            throws Exception {

        MultipartFile file = createFile(
                "email-logo.svg",
                "image/svg+xml",
                1024
        );

        when(storageClient.upload(
                file,
                "branding/email-header-logo"
        )).thenReturn("email-logo-reference");

        String result =
                brandingAssetService
                        .uploadEmailHeaderLogo(file);

        assertEquals(
                "email-logo-reference",
                result
        );

        verify(storageClient)
                .upload(
                        file,
                        "branding/email-header-logo"
                );
    }

    @Test
    void uploadEmailHeaderLogo_shouldRejectUnsupportedExtension() {

        MultipartFile file = createFile(
                "email-logo.gif",
                "image/gif",
                1024
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> brandingAssetService
                        .uploadEmailHeaderLogo(file)
        );

        verifyNoInteractions(storageClient);
    }

    @Test
    void replaceEmailHeaderLogo_shouldReplaceSuccessfully()
            throws Exception {

        MultipartFile file = createFile(
                "email-logo.png",
                "image/png",
                1024
        );

        when(storageClient.replace(
                "old-email-logo",
                file,
                "branding/email-header-logo"
        )).thenReturn("new-email-logo");

        String result =
                brandingAssetService
                        .replaceEmailHeaderLogo(
                                "old-email-logo",
                                file
                        );

        assertEquals(
                "new-email-logo",
                result
        );

        verify(storageClient)
                .replace(
                        "old-email-logo",
                        file,
                        "branding/email-header-logo"
                );
    }

    @Test
    void deleteEmailHeaderLogo_shouldDeleteSuccessfully()
            throws Exception {

        brandingAssetService.deleteEmailHeaderLogo(
                "email-logo-reference"
        );

        verify(storageClient)
                .delete("email-logo-reference");
    }

    // ============================================================
    // STORAGE ERROR TESTS
    // ============================================================

    @Test
    void uploadLogo_shouldThrowIllegalStateExceptionWhenStorageFails()
            throws Exception {

        MultipartFile file = createFile(
                "logo.png",
                "image/png",
                1024
        );

        when(storageClient.upload(
                file,
                "branding/logo"
        )).thenThrow(
                new IOException("Storage unavailable")
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> brandingAssetService
                                .uploadLogo(file)
                );

        assertEquals(
                "Failed to store logo",
                exception.getMessage()
        );

        verify(storageClient)
                .upload(
                        file,
                        "branding/logo"
                );
    }

    @Test
    void replaceLogo_shouldThrowIllegalStateExceptionWhenStorageFails()
            throws Exception {

        MultipartFile file = createFile(
                "logo.png",
                "image/png",
                1024
        );

        when(storageClient.replace(
                "old-logo",
                file,
                "branding/logo"
        )).thenThrow(
                new IOException("Storage unavailable")
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> brandingAssetService
                                .replaceLogo(
                                        "old-logo",
                                        file
                                )
                );

        assertEquals(
                "Failed to replace logo",
                exception.getMessage()
        );
    }

    @Test
    void deleteLogo_shouldThrowIllegalStateExceptionWhenStorageFails()
            throws Exception {

        doThrow(
                new IOException("Storage unavailable")
        ).when(storageClient)
                .delete("logo-reference");

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> brandingAssetService
                                .deleteLogo(
                                        "logo-reference"
                                )
                );

        assertEquals(
                "Failed to delete logo",
                exception.getMessage()
        );
    }

    // ============================================================
    // HELPER
    // ============================================================

    private MultipartFile createFile(
            String filename,
            String contentType,
            long size) {

        byte[] content =
                new byte[(int) size];

        return new MockMultipartFile(
                "file",
                filename,
                contentType,
                content
        );
    }
}