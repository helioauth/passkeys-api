package com.helioauth.passkeys.api.contract;

public record RegisterCredentialRequest(
    String requestId,
    String publicKeyCredential,
    String username
) { }
