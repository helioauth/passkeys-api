package com.helioauth.passkeys.api.contract;

public record StartAssertionResponse(String requestId, String credentialsGetOptions) { }
