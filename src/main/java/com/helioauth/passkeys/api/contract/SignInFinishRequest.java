package com.helioauth.passkeys.api.contract;

public record SignInFinishRequest(
    String requestId,
    String publicKeyCredentialWithAssertion
) {
}
