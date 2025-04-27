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

import lombok.Builder;
import lombok.Value;

/**
 * Data Transfer Object for starting a user signup process.
 */
@Value
@Builder
public class UserSignupStartRequest {
    /**
     * The username for the signup.
     */
    String name;
    
    /**
     * The Relying Party ID (typically the domain name).
     */
    String rpId;
    
    /**
     * The Relying Party name. Can be null.
     */
    String rpName;
    
    /**
     * Creates a builder with name and rpId set.
     */
    public static UserSignupStartRequestBuilder withNameAndRpId(String name, String rpId) {
        return builder().name(name).rpId(rpId);
    }
}