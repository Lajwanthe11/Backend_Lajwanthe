package com.example.platformadmin.superadmin.platformconfiguration.exception;

/**
 * Raised when a platform configuration activation/deactivation operation fails.
 *
 * <p><b>FRS Traceability:</b></p>
 * <ul>
 *   <li>VAL-0012 &mdash; Configuration shall pass validation before activation.</li>
 *   <li>ERR-0009 &mdash; Configuration activation failed (Roll back configuration).</li>
 * </ul>
 */
public class ConfigurationActivationException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new configuration activation exception.
     *
     * @param message description of the activation or deactivation failure
     */
    public ConfigurationActivationException(String message) {
        super(message);
    }
}