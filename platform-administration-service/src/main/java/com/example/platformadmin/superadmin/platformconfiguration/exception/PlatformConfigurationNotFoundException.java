package com.example.platformadmin.superadmin.platformconfiguration.exception;


/**
 * Raised when a requested Platform Configuration does not exist or has been soft deleted.
 *
 * <p><b>FRS Traceability:</b> FR-001.2 (Platform Configuration), ERR-0011 (Resource Not Found).</p>
 *
 * <p>Handled by {@link GlobalExceptionHandler} and returned to the client as HTTP 404 NOT FOUND.</p>
 */
public class PlatformConfigurationNotFoundException
        extends RuntimeException {

    /**
	     * 
	     */
	    private static final long serialVersionUID = 1L;

	/**
     * Creates an exception with the specified detail message.
     *
     * @param message description of the missing platform configuration
     */
    public PlatformConfigurationNotFoundException(String message) {
        super(message);
    }
}