package com.helioauth.passkeys.api.service.dto;

import java.io.Serializable;
import java.time.Instant;

public record CredentialAssertionResultDto(
        long signatureCount,
        Instant lastUsedAt,
        boolean isBackedUp,
        String userHandle,
        String credentialId,
        String username
) implements Serializable {
}
