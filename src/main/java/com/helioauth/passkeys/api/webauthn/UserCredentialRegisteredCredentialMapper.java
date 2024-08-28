package com.helioauth.passkeys.api.webauthn;

import com.helioauth.passkeys.api.domain.UserCredential;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
