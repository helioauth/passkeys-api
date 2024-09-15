/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.helioauth.passkeys.api.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Viktor Stanchev
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "There was a problem signing you up. Please, try again.")
public class CredentialRegistrationFailedException extends RuntimeException {
    public CredentialRegistrationFailedException(String message) {
        super(message);
    }

    public CredentialRegistrationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
