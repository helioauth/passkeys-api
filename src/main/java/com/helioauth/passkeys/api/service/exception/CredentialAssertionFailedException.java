package com.helioauth.passkeys.api.service.exception;

public class CredentialAssertionFailedException extends RuntimeException {
    public CredentialAssertionFailedException() {
        super("Couldn't find an account with this email and passkey.");
    }
}
