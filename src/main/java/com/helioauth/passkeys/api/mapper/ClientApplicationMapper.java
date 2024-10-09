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

import com.helioauth.passkeys.api.domain.ClientApplication;
import com.helioauth.passkeys.api.generated.models.Application;
import com.helioauth.passkeys.api.generated.models.ApplicationApiKey;
import com.helioauth.passkeys.api.service.dto.ClientApplicationApiKeyDTO;
import com.helioauth.passkeys.api.service.dto.ClientApplicationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * @author Viktor Stanchev
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientApplicationMapper {
    ClientApplicationDTO toDto(ClientApplication clientApplication);
    List<ClientApplicationDTO> toDto(List<ClientApplication> clientApplication);

    ClientApplicationApiKeyDTO toApiKeyDto(ClientApplication clientApplication);

    Application toApplicationResponse(ClientApplicationDTO clientApplicationDTO);
    List<Application> toApplicationResponse(List<ClientApplicationDTO> clientApplicationDTO);

    ApplicationApiKey toApplicationApiKeyResponse(ClientApplicationApiKeyDTO clientApplicationApiKeyDTO);
}
