package com.example.auth.securityalerts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** "Resolve Alert". */
public record ResolveAlertRequest(
        @NotBlank(message = "Resolution remarks are mandatory")
        @Size(max = 1000, message = "Resolution remarks can be at most 1000 characters")
        String resolutionRemarks
) {
}
