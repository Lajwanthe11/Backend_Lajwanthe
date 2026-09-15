package com.example.auth.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

    @Service
    public class SessionManagementService {

        private static final long SESSION_TIMEOUT_MINUTES = 15;

        private final Map<String, Session> sessions = new ConcurrentHashMap<>();

        public String createSession(String username, String tenantId) {

            String sessionId = UUID.randomUUID().toString();

            Session session = new Session(
                    sessionId,
                    username,
                    tenantId,
                    LocalDateTime.now(),
                    true
            );

            sessions.put(sessionId, session);

            return sessionId;
        }

        public boolean isSessionActive(String sessionId) {

            Session session = sessions.get(sessionId);

            if (session == null || !session.active()) {
                return false;
            }

            if (session.createdAt()
                    .plusMinutes(SESSION_TIMEOUT_MINUTES)
                    .isBefore(LocalDateTime.now())) {

                invalidateSession(sessionId);
                return false;
            }

            return true;
        }

        public void invalidateSession(String sessionId) {

            Session session = sessions.get(sessionId);

            if (session != null) {
                sessions.put(
                        sessionId,
                        new Session(
                                session.sessionId(),
                                session.username(),
                                session.tenantId(),
                                session.createdAt(),
                                false
                        )
                );
            }
        }

        public Session getSession(String sessionId) {
            return sessions.get(sessionId);
        }

        public record Session(
                String sessionId,
                String username,
                String tenantId,
                LocalDateTime createdAt,
                boolean active
        ) {
        }
    }

