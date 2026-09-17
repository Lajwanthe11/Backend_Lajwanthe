package com.example.platformadmin.superadmin.platformconfiguration.exception;

/**
 * Raised when an operation is rejected because the caller is not authorized.
 *
 * <p><b>FRS Traceability:</b></p>
 * <ul>
 *   <li>BR-0013 &mdash; Only Super Administrators shall modify platform configurations.</li>
 *   <li>AC-0014 &mdash; Unauthorized users shall not modify platform configurations.</li>
 *   <li>ERR-0011 &mdash; Unauthorized configuration access. Deny access.</li>
 * </ul>
 */
public class UnauthorizedConfigurationAccessException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new unauthorized configuration access exception.
     *
     * @param message description of the authorization failure
     */
    public UnauthorizedConfigurationAccessException(String message) {
        super(message);
    }
}