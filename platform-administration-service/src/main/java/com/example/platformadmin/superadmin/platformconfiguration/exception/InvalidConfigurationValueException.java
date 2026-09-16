package com.example.platformadmin.superadmin.platformconfiguration.exception;

/**
 * Raised when business-level configuration validation fails.
 *
 * <p><b>FRS Traceability:</b></p>
 * <ul>
 *   <li>BR-0017 &mdash; Invalid configuration values shall not be saved.</li>
 *   <li>ERR-0008 &mdash; Invalid configuration value error handling.</li>
 * </ul>
 */
public class InvalidConfigurationValueException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new invalid configuration value exception with the given message.
     *
     * @param message detail explanation of the validation failure
     */
    public InvalidConfigurationValueException(String message) {
        super(message);
    }
}