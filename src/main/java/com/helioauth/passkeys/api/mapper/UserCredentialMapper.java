package com.helioauth.passkeys.api.mapper;

import com.helioauth.passkeys.api.domain.UserCredential;
import com.helioauth.passkeys.api.service.dto.CredentialRegistrationResultDto;
import com.helioauth.passkeys.api.service.dto.UserCredentialDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserCredentialMapper {
    List<UserCredentialDTO> toDto(List<UserCredential> userCredentialList);

    @Mapping(target = "lastUsedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    UserCredential fromCredentialRegistrationResult(CredentialRegistrationResultDto registrationResultDto);
}