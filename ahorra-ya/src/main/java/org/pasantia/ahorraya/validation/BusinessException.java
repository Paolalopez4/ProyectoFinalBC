package org.pasantia.ahorraya.validation;

/**
 * Generic runtime exception for domain/business rule violations.
 *
 * <p>Use this exception to indicate business-level errors that should be
 * propagated to higher layers (services/controllers). Messages supplied to
 * the constructors should be human-readable and in English, suitable for
 * logging or returning to API clients.</p>
 */
public class BusinessException extends RuntimeException {

    /**
     * Create a new BusinessException with the specified detail message.
     *
     * @param message a human-readable message in English describing the business error
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Create a new BusinessException with the specified detail message and cause.
     *
     * @param message a human-readable message in English describing the business error
     * @param cause   the underlying cause of this exception
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
