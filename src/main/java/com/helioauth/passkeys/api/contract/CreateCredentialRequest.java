package com.helioauth.passkeys.api.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateCredentialRequest(@JsonProperty String name) {
    public CreateCredentialRequest {
        name = name.strip().toLowerCase();
    }
}
