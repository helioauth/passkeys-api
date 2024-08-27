package com.helioauth.passkeys.api.contract;

public record StartAssertionRequest(String name) {
    public StartAssertionRequest(String name) {
        this.name = name.strip().toLowerCase();
    }
}
