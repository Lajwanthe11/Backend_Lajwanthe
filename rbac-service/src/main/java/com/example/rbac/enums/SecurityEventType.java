package com.example.rbac.enums;

/**
 * Type of entry recorded in {@code security_events}. Part 8 currently only
 * ever writes {@link #ACCESS_DENIED} rows (403s) - kept as its own enum,
 * rather than a raw string, so it's easy to extend later without touching
 * every call site.
 */
public enum SecurityEventType {
    ACCESS_DENIED
}
