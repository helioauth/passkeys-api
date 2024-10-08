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

package com.helioauth.passkeys.api.webauthn;

import com.helioauth.passkeys.api.domain.User;
import com.helioauth.passkeys.api.domain.UserCredential;
import com.helioauth.passkeys.api.domain.UserCredentialRepository;
import com.helioauth.passkeys.api.domain.UserRepository;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Viktor Stanchev
 */
@Service
@RequiredArgsConstructor
public class DatabaseCredentialRepository implements CredentialRepository {

    private final UserRepository userRepository;

    private final UserCredentialRepository userCredentialRepository;

    private final UserCredentialRegisteredCredentialMapper userCredentialRegisteredCredentialMapper;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String s) {
        Optional<User> user = userRepository.findByName(s);
        if (user.isEmpty()) {
            return Collections.emptySet();
        }

        List<UserCredential> userCredentials = user.get().getUserCredentials();
        Set<PublicKeyCredentialDescriptor> result = HashSet.newHashSet(userCredentials.size());

        for (UserCredential userCredential : userCredentials) {
            result.add(
                    PublicKeyCredentialDescriptor.builder()
                            .id(ByteArray.fromBase64(userCredential.getCredentialId()))
                            .type(PublicKeyCredentialType.PUBLIC_KEY)
                            .transports(Set.of(AuthenticatorTransport.INTERNAL))
                            .build()
            );
        }

        return result;
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String s) {
        Optional<UserCredential> userCredential = userCredentialRepository.findFirstByUserName(s);
        return userCredential.map(credential -> ByteArray.fromBase64(credential.getUserHandle()));

    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray byteArray) {
        Optional<User> user = userRepository.findFirstByUserCredentialsUserHandle(byteArray.getBase64());
        return user.map(User::getName);
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        Optional<UserCredential> optional = userCredentialRepository.findFirstByUserHandleAndCredentialId(
                userHandle.getBase64(),
                credentialId.getBase64()
        );

        return optional.map(userCredentialRegisteredCredentialMapper::toRegisteredCredential);
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        List<UserCredential> credentialsList = userCredentialRepository.findAllByUserHandle(
                credentialId.getBase64()
        );

        return userCredentialRegisteredCredentialMapper.toRegisteredCredentialSet(credentialsList);
    }
}
