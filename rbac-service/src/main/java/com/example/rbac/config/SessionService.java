package com.example.rbac.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class SessionService {

    public void invalidateUserSessions(UUID userId) {
        // Invalidate all active sessions for the user.
        // Integrate with session registry or token store as needed.
        log.info("Sessions invalidated for user {}", userId);
    }
}
