package com.helioauth.passkeys.api.contract;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateCredentialResponse {
    String requestId;
    String publicKeyCredentialCreationOptions;
}
