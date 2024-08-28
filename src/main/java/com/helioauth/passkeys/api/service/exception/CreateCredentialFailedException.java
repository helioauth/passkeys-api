package com.helioauth.passkeys.api.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "There was a problem creating a credential. Please, try again.")
public class CreateCredentialFailedException extends RuntimeException {

}
