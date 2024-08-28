package com.helioauth.passkeys.api.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StartAssertionRequest(@JsonProperty String name) {
    public StartAssertionRequest {
        name = name.strip().toLowerCase();
    }
}
