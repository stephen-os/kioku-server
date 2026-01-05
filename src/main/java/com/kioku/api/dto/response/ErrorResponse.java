package com.kioku.api.dto.response;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for error response.
 *
 * <p>This response object is returned when an error occurs during request processing.
 * It provides a user-friendly error message and a timestamp of when the error occurred.
 *
 * <p><strong>Response Fields:</strong>
 * <ul>
 *   <li>message: Human-readable error message</li>
 *   <li>timestamp: When the error occurred (automatically set to current time)</li>
 * </ul>
 *
 * <p><strong>Common Use Cases:</strong>
 * <ul>
 *   <li>Validation errors (400 Bad Request)</li>
 *   <li>Authentication errors (401 Unauthorized)</li>
 *   <li>Resource not found (404 Not Found)</li>
 *   <li>Server errors (500 Internal Server Error)</li>
 * </ul>
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "message": "Email already in use",
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class ErrorResponse {

    /**
     * The human-readable error message.
     * This message should be suitable for display to end users.
     */
    private String message;

    /**
     * The timestamp when the error occurred.
     * Automatically set to the current time when the error response is created.
     */
    private LocalDateTime timestamp;

    /**
     * Constructs an ErrorResponse with no message.
     * The timestamp is automatically set to the current time.
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructs an ErrorResponse with the specified message.
     * The timestamp is automatically set to the current time.
     *
     * @param message the error message
     */
    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the error message.
     *
     * @param message the error message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the timestamp when the error occurred.
     *
     * @return the error timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp when the error occurred.
     *
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}