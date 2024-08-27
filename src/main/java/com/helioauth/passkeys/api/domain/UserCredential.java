package com.helioauth.passkeys.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
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

@Entity
@Table
@Builder
@Getter
@Setter
@AllArgsConstructor
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

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected UserCredential() {

    }
}
