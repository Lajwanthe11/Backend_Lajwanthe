package com.example.platformadmin.superadmin.platformconfiguration.exception;

/**
 * Raised when a platform configuration key conflicts with another
 * non-deleted configuration.
 *
 * <p>
 * FRS traceability:
 * <ul>
 *     <li>Validation: VAL-0007 — Configuration Name shall be unique.</li>
 *     <li>Error: ERR-0007 — Duplicate Configuration Name.</li>
 * </ul>
 *
 * <p>
 * The approved SQL persistence model exposes the corresponding field as
 * {@code config_key}.
 * </p>
 */
public class DuplicateConfigurationNameException
        extends RuntimeException {

    /**
     * Constructs a new duplicate configuration name exception with the conflicting name.
     *
     * @param message conflicting configuration name or detail description
     */
    public DuplicateConfigurationNameException(String message) {
        super(message);
    }
}