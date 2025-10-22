package org.pasantia.ahorraya.validation;

import lombok.extern.slf4j.Slf4j;
import org.pasantia.ahorraya.dto.response.ValidationErrorResponse;
import org.pasantia.ahorraya.dto.response.ErrorResponse;
import org.pasantia.ahorraya.validation.expensetransactionvalidations.ExpenseTransactionNotFoundException;
import org.pasantia.ahorraya.validation.expensetransactionvalidations.InvalidRoundingAmountException;
import org.pasantia.ahorraya.validation.microsavingconfigvalidations.ConfigAlreadyActiveException;
import org.pasantia.ahorraya.validation.microsavingconfigvalidations.MicroSavingConfigNotFoundException;
import org.pasantia.ahorraya.validation.savingaccountvalidations.AccountAlreadyDeletedException;
import org.pasantia.ahorraya.validation.savingaccountvalidations.AccountAlreadyExistsException;
import org.pasantia.ahorraya.validation.savingaccountvalidations.AccountNotFoundException;
import org.pasantia.ahorraya.validation.savingaccountvalidations.InactiveAccountException;
import org.pasantia.ahorraya.validation.savingmovementvalidations.CannotRevertMovementException;
import org.pasantia.ahorraya.validation.savingmovementvalidations.InsufficientBalanceException;
import org.pasantia.ahorraya.validation.savingmovementvalidations.InvalidAmountException;
import org.pasantia.ahorraya.validation.savingmovementvalidations.InvalidMovementStatusException;
import org.pasantia.ahorraya.validation.uservalidations.DuplicateEmailException;
import org.pasantia.ahorraya.validation.uservalidations.DuplicateIdentificationException;
import org.pasantia.ahorraya.validation.uservalidations.DuplicateUsernameException;
import org.pasantia.ahorraya.validation.uservalidations.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler that translates application exceptions into HTTP responses.
 *
 * <p>Handles domain/business exceptions, validation failures, authentication/authorization
 * errors and general server errors, returning structured JSON error payloads.</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler({
            DuplicateEmailException.class,
            DuplicateUsernameException.class,
            DuplicateIdentificationException.class,
            AccountAlreadyExistsException.class,
            ConfigAlreadyActiveException.class,
            InvalidAmountException.class,
            InvalidRoundingAmountException.class,
            InsufficientBalanceException.class,
            InactiveAccountException.class,
            AccountAlreadyDeletedException.class,
            InvalidMovementStatusException.class,
            CannotRevertMovementException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    /**
     * Handle business/domain exceptions and return HTTP 400 with the exception message.
     *
     * @param ex the business exception
     * @param request the current web request
     * @return ResponseEntity containing an ErrorResponse with status 400
     */
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {

        log.warn("Business exception: {} - Path: {}",
                ex.getMessage(), request.getDescription(false));

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(extractPath(request))
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler({
            UserNotFoundException.class,
            AccountNotFoundException.class,
            ExpenseTransactionNotFoundException.class,
            MicroSavingConfigNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    /**
     * Handle not-found exceptions and return HTTP 404 with the exception message.
     *
     * @param ex the not-found business exception
     * @param request the current web request
     * @return ResponseEntity containing an ErrorResponse with status 404
     */
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            BusinessException ex, WebRequest request) {

        log.warn("Resource not found: {} - Path: {}",
                ex.getMessage(), request.getDescription(false));

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(extractPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler({
            AuthenticationException.class,
            BadCredentialsException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    /**
     * Handle authentication errors and return HTTP 401 with a generic message.
     *
     * @param ex the authentication exception
     * @param request the current web request
     * @return ResponseEntity containing an ErrorResponse with status 401
     */
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            Exception ex, WebRequest request) {

        log.warn("Authentication error: {} - Path: {}",
                ex.getMessage(), request.getDescription(false));

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Invalid credentials or expired token")
                .path(extractPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    /**
     * Handle access denied errors and return HTTP 403 with a generic message.
     *
     * @param ex the access denied exception
     * @param request the current web request
     * @return ResponseEntity containing an ErrorResponse with status 403
     */
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        log.warn("Access denied: {} - Path: {}",
                ex.getMessage(), request.getDescription(false));

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("You do not have permission to access this resource")
                .path(extractPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    /**
     * Handle validation failures for @Valid annotated request bodies and return HTTP 400
     * containing field-level error messages.
     *
     * @param ex the MethodArgumentNotValidException
     * @param request the current web request
     * @return ResponseEntity containing a ValidationErrorResponse with details
     */
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation errors: {} - Path: {}",
                errors, request.getDescription(false));

        ValidationErrorResponse error = ValidationErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Validation error in request fields")
                .path(extractPath(request))
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    /**
     * Handle IllegalArgumentException and return HTTP 400 with the exception message.
     *
     * @param ex the IllegalArgumentException
     * @param request the current web request
     * @return ResponseEntity containing an ErrorResponse with status 400
     */
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        log.warn("Invalid argument: {} - Path: {}",
                ex.getMessage(), request.getDescription(false));

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(extractPath(request))
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    /**
     * Handle method argument type mismatches and return HTTP 400 with a clear message.
     *
     * @param ex the MethodArgumentTypeMismatchException
     * @param request the current web request
     * @return ResponseEntity containing an ErrorResponse with status 400
     */
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        String message = String.format("Parameter '%s' must be of type %s",
                ex.getName(), ex.getRequiredType().getSimpleName());

        log.warn("Argument type error: {} - Path: {}",
                message, request.getDescription(false));

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .path(extractPath(request))
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    /**
     * Handle IllegalStateException and return HTTP 409 with the exception message.
     *
     * @param ex the IllegalStateException
     * @param request the current web request
     * @return ResponseEntity containing an ErrorResponse with status 409
     */
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {

        log.warn("Illegal state: {} - Path: {}",
                ex.getMessage(), request.getDescription(false));

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .path(extractPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    /**
     * Catch-all handler for unexpected exceptions. Returns HTTP 500 with a generic message.
     *
     * @param ex the unexpected exception
     * @param request the current web request
     * @return ResponseEntity containing an ErrorResponse with status 500
     */
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {

        log.error("Internal server error: {} - Path: {}",
                ex.getMessage(), request.getDescription(false), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An internal error has occurred. Please contact the administrator.")
                .path(extractPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.replace("uri=", "");
    }
}
