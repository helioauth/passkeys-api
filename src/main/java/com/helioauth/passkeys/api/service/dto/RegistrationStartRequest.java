/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.helioauth.passkeys.api.service.dto;

import com.yubico.webauthn.data.ByteArray;
import lombok.Builder;
import lombok.Value;

/**
 * Data Transfer Object for starting a WebAuthn registration.
 */
@Value
@Builder
public class RegistrationStartRequest {
    /**
     * The username for the registration.
     */
    String name;
    
    /**
     * The user ID for the registration. If null, a random ID will be generated.
     */
    ByteArray userId;
    
    /**
     * The Relying Party hostname. If null, the default from properties will be used.
     */
    String rpHostname;
    
    /**
     * The Relying Party name. Can be null.
     */
    String rpName;
    
    /**
     * Creates a builder with only the name set.
     */
    public static RegistrationStartRequestBuilder withName(String name) {
        return builder().name(name);
    }
}