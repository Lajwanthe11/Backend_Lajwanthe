package com.example.auth.securityalerts.service;

import com.example.auth.securityalerts.entity.SecurityEvent;

import java.util.List;

/**
 * Threat Detection Engine: rules that look across a user's event history and report what no
 * single module can see on its own, such as sign-ins from several IP addresses at once.
 */
public interface ThreatDetectionService {

    /**
     * Runs the detection rules for an incoming event, before that event is saved.
     * Returns the derived events (not saved) that the engine should process next.
     */
    List<SecurityEvent> detect(SecurityEvent event);
}
