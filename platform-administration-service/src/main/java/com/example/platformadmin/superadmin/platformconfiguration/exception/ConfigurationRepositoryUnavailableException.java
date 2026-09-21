package com.example.platformadmin.superadmin.platformconfiguration.exception;

/**
 * Raised when the Platform Configuration repository is unavailable.
 *
 * <p>
 * FRS reference: ERR-0012.
 * </p>
 */
public class ConfigurationRepositoryUnavailableException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new repository unavailable exception.
     *
     * @param message description of the repository availability failure
     */
    public ConfigurationRepositoryUnavailableException(String message) {
        super(message);
    }
}