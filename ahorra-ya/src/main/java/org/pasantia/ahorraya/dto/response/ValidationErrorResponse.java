package org.pasantia.ahorraya.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Standard response payload returned when request validation fails.
 *
 * <p>This DTO carries a timestamp, HTTP status code, a short error reason,
 * a human-readable message intended for clients (in English), the request path
 * that produced the error, and a map of field-specific validation error messages
 * keyed by field name.</p>
 */
@Data
@Builder
public class ValidationErrorResponse {

    /**
     * UTC timestamp of the error event formatted as ISO-8601 with milliseconds.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    /**
     * HTTP status code associated with the validation error (for example: 400).
     */
    private int status;

    /**
     * Short error reason (for example: "Bad Request").
     */
    private String error;

    /**
     * Human-readable message describing the error; intended for clients (English).
     */
    private String message;

    /**
     * The request path where the validation error occurred (for example: "/api/users").
     */
    private String path;

    /**
     * Map of field names to validation error messages. Messages should be English and
     * explain the specific field validation failures.
     */
    private Map<String, String> validationErrors;
}
