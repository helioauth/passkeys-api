package com.helioauth.passkeys.api.contract;

public record SignInValidateKeyRequest(
    String requestId,
    String publicKeyCredentialWithAssertion
) {
}
