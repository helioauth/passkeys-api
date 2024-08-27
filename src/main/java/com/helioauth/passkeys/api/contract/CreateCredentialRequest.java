package com.helioauth.passkeys.api.contract;

public record CreateCredentialRequest(String name) {
    public CreateCredentialRequest(String name) {
        this.name = name.strip().toLowerCase();
    }
}
