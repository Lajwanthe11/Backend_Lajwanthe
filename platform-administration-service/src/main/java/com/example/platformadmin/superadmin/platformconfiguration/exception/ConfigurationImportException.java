package com.example.platformadmin.superadmin.platformconfiguration.exception;

/**
 * Raised when a configuration import operation fails.
 *
 * <p><b>FRS Traceability:</b> ERR-0010 &mdash; Configuration import failed.</p>
 */
public class ConfigurationImportException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new import exception with the given detail message.
     *
     * @param message detail explanation of the import failure
     */
    public ConfigurationImportException(String message) {
        super(message);
    }

}