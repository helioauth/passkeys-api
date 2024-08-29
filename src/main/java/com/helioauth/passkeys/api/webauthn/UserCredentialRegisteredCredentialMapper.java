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

import com.helioauth.passkeys.api.domain.UserCredential;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Viktor Stanchev
 */
@Component
public class UserCredentialRegisteredCredentialMapper {

    public RegisteredCredential toRegisteredCredential(UserCredential userCredential) {
        return RegisteredCredential.builder()
                .credentialId(ByteArray.fromBase64(userCredential.getCredentialId()))
                .userHandle(ByteArray.fromBase64(userCredential.getUserHandle()))
                .publicKeyCose(ByteArray.fromBase64(userCredential.getPublicKeyCose()))
                .signatureCount(userCredential.getSignatureCount())
                .build();
    }

    public Set<RegisteredCredential> toRegisteredCredentialSet(List<UserCredential> userCredentialList) {
        return userCredentialList.stream().map(this::toRegisteredCredential).collect(Collectors.toSet());
    }
}
