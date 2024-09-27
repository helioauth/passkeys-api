package com.helioauth.passkeys.api.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientApplicationRepository extends JpaRepository<ClientApplication, UUID> {
}