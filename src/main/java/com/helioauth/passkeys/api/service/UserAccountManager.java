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

package com.helioauth.passkeys.api.service;

import com.helioauth.passkeys.api.domain.UserRepository;
import com.helioauth.passkeys.api.service.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Viktor Stanchev
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountManager {
    private final UserRepository userRepository;

    public void deleteUser(UUID userUuid) {
        if (userUuid == null || !userRepository.existsById(userUuid)) {
            throw new UserNotFoundException();
        }

        userRepository.deleteById(userUuid);
    }
}
