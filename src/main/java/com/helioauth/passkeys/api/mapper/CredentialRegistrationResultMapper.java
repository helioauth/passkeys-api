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

package com.helioauth.passkeys.api.mapper;

import com.helioauth.passkeys.api.service.dto.CredentialRegistrationResultDto;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.UserIdentity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * @author Viktor Stanchev
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CredentialRegistrationResultMapper {

    @Mapping(target = "credentialId", source = "registrationResult.keyId.id.base64")
    @Mapping(target = "userHandle", source = "userIdentity.id.base64")
    @Mapping(target = "publicKeyCose", source = "registrationResult.publicKeyCose.base64")
    @Mapping(target = "backupState", source = "registrationResult.backedUp")
    @Mapping(target = "isDiscoverable", expression = "java(registrationResult.isDiscoverable().orElse(false))")
    @Mapping(target = "attestationObject", source = "authenticatorResponse.attestationObject.base64")
    @Mapping(target = "clientDataJson", source = "authenticatorResponse.clientDataJSON.base64")
    CredentialRegistrationResultDto fromRegistrationResult(RegistrationResult registrationResult, UserIdentity userIdentity, AuthenticatorAttestationResponse authenticatorResponse);
}
