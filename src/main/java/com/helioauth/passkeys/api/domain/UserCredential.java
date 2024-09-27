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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * @author Viktor Stanchev
 */
@Entity
@Table(name = "user_credentials")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String credentialId;

    @Column
    private String userHandle;

    @Column
    private String displayName;

    @Column
    private Long signatureCount;

    @Column(columnDefinition = "text")
    private String publicKeyCose;

    @Column(columnDefinition = "text")
    private String attestationObject;

    @Column(columnDefinition = "text")
    private String clientDataJson;

    @Column
    private Boolean backupEligible;

    @Column
    private Boolean backupState;

    @Column
    private Boolean isDiscoverable;

    @Column
    private Instant lastUsedAt;

    @ManyToOne(targetEntity = User.class, fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
