package com.helioauth.passkeys.api.service.dto;

public record CredentialRegistrationResultDto(
        String name,
        String displayName,
        String credentialId,
        String userHandle,
        Long signatureCount,
        String publicKeyCose,
        String attestationObject,
        String clientDataJson,
        Boolean backupEligible,
        Boolean backupState,
        Boolean isDiscoverable
) {
}