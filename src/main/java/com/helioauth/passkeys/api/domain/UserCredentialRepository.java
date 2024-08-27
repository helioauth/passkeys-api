package com.helioauth.passkeys.api.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    Optional<UserCredential> findFirstByUserName(String username);

    List<UserCredential> findAllByUserName(String username);

    List<UserCredential> findAllByUserHandle(String base64EncodedUserHandle);

    Optional<UserCredential> findFirstByUserHandleAndCredentialId(String base64UserHandle, String base64CredentialId);

    @Modifying(flushAutomatically = true)
    @Query("update UserCredential uc set uc.signatureCount = ?1, uc.lastUsedAt = ?2, uc.backupState = ?3 where uc.userHandle = ?4 and uc.credentialId = ?5")
    void updateUsageByUserHandleAndCredentialId(Long signatureCount, Instant lastUsedAt, Boolean backupState, String userHandle, String credentialId);
}
