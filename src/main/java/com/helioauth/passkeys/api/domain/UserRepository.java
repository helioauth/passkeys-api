package com.helioauth.passkeys.api.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByName(String name);

    Optional<User> findFirstByUserCredentialsUserHandle(String base64EncodedUserHandle);
}
