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

package com.helioauth.passkeys.api.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Viktor Stanchev
 */
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    Optional<UserCredential> findFirstByUserName(String username);

    List<UserCredential> findAllByUserId(UUID userUuid);

    List<UserCredential> findAllByUserHandle(String base64EncodedUserHandle);

    Optional<UserCredential> findFirstByUserHandleAndCredentialId(String base64UserHandle, String base64CredentialId);

    @Modifying(flushAutomatically = true)
    @Query("update UserCredential uc set uc.signatureCount = ?1, uc.lastUsedAt = ?2, uc.backupState = ?3 where uc.userHandle = ?4 and uc.credentialId = ?5")
    void updateUsageByUserHandleAndCredentialId(Long signatureCount, Instant lastUsedAt, Boolean backupState, String userHandle, String credentialId);
}
