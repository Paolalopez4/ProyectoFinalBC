package org.pasantia.ahorraya.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Standard error response returned by API endpoints when an error occurs.
 *
 * <p>Includes a machine-readable timestamp, HTTP status code, a short error
 * reason, a human-readable message suitable for the client, and the request path
 * where the error happened.</p>
 */
@Data
@Builder
public class ErrorResponse {

    /**
     * Timestamp of the error event in UTC, formatted as ISO-8601 with milliseconds.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    /**
     * HTTP status code associated with the error (for example: 400, 404, 500).
     */
    private int status;

    /**
     * Short error reason or type (for example: "Bad Request", "Not Found").
     */
    private String error;

    /**
     * Human-readable detail explaining the error; intended for clients.
     */
    private String message;

    /**
     * The request path that produced the error (for example: "/api/users/123").
     */
    private String path;
}