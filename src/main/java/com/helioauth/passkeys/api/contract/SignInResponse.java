package com.helioauth.passkeys.api.contract;

public record SignInResponse(
    String requestId,
    String username
) {
}