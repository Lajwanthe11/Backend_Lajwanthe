package com.example.auth.loginhistory.exception;

/**
 * Thrown when a Login History operation cannot complete because of an underlying
 * infrastructure failure (database access, CSV generation, session bookkeeping, etc.)
 * rather than bad caller input.
 * Mapped to 500 Internal Server Error by {@link LoginHistoryGlobalExceptionHandler}.
 */
public class LoginHistoryException extends RuntimeException {

    public LoginHistoryException(String message) {
        super(message);
    }

    public LoginHistoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /** A read query (search/get/summary) failed, typically due to a database problem. */
    public static LoginHistoryException queryFailed(String operation, Throwable cause) {
        return new LoginHistoryException(
                "Failed to " + operation + " login history: " + rootMessage(cause), cause);
    }

    /** CSV report generation failed after the underlying records were successfully fetched. */
    public static LoginHistoryException exportFailed(Throwable cause) {
        return new LoginHistoryException(
                "Failed to generate login history CSV export: " + rootMessage(cause), cause);
    }

    /** A session state change (force-logout, auto-close, etc.) failed. */
    public static LoginHistoryException sessionOperationFailed(String operation, Throwable cause) {
        return new LoginHistoryException(
                "Failed to " + operation + ": " + rootMessage(cause), cause);
    }

    /** Unwraps to the root cause's message so the response doesn't just say "DataAccessException". */
    private static String rootMessage(Throwable cause) {
        Throwable root = cause;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName();
    }
}
