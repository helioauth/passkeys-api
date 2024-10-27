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

import com.helioauth.passkeys.api.domain.UserCredential;
import com.helioauth.passkeys.api.service.dto.CredentialRegistrationResult;
import com.helioauth.passkeys.api.service.dto.PasskeyItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * @author Viktor Stanchev
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserCredentialMapper {
    List<PasskeyItem> toDto(List<UserCredential> userCredentialList);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUsedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    UserCredential fromCredentialRegistrationResult(CredentialRegistrationResult registrationResultDto);
}