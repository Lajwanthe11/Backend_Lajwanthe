package com.example.auth.securityalerts.dto;

import jakarta.validation.constraints.Size;

/** Optional remarks for acknowledge and close. Closing needs remarks unless the alert was already resolved with some. */
public record AlertActionRequest(
        @Size(max = 1000, message = "Remarks can be at most 1000 characters") String remarks
) {
}
