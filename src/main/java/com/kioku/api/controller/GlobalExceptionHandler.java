package com.kioku.api.controller;

import com.kioku.api.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Global exception handler for all REST controllers.
 *
 * <p>This class provides centralized exception handling across all
 * {@code @RestController} classes in the application. It catches exceptions
 * thrown by controllers and converts them into appropriate HTTP responses.
 *
 * <p><strong>Handled Exceptions:</strong>
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} - Validation errors (400 Bad Request)</li>
 *   <li>{@link HttpMessageNotReadableException} - Malformed JSON (400 Bad Request)</li>
 *   <li>{@link IllegalArgumentException} - Business logic errors (400 Bad Request)</li>
 *   <li>{@link NoSuchElementException} - Resource not found (404 Not Found)</li>
 *   <li>{@link AccessDeniedException} - Authorization errors (403 Forbidden)</li>
 *   <li>{@link Exception} - Unexpected errors (500 Internal Server Error)</li>
 * </ul>
 *
 * <p><strong>Response Formats:</strong>
 * <ul>
 *   <li>Validation errors: Map of field names to error messages</li>
 *   <li>Other errors: {@link ErrorResponse} object with error message</li>
 * </ul>
 *
 * <p><strong>Usage:</strong>
 * Controllers do not need to catch exceptions explicitly. This handler
 * automatically intercepts exceptions and returns appropriate responses.
 *
 * <p><strong>Example:</strong>
 * <pre>
 * // Controller code - no try-catch needed
 * {@code @PostMapping("/api/decks")}
 * public ResponseEntity{@code <DeckResponse>} createDeck(...) {
 *     Deck deck = deckService.createDeck(...); // May throw IllegalArgumentException
 *     return ResponseEntity.status(HttpStatus.CREATED).body(new DeckResponse(deck));
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation errors from {@code @Valid} annotations.
     *
     * <p>This method is triggered when request body validation fails.
     * It returns a map of field names to error messages.
     *
     * <p><strong>Response Format:</strong>
     * <pre>
     * {
     *   "fieldName": "Error message",
     *   "anotherField": "Another error message"
     * }
     * </pre>
     *
     * @param ex the validation exception
     * @return ResponseEntity with 400 status and field error map
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        logger.debug("Validation error occurred: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            logger.debug("Validation error - Field: {}, Message: {}", fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles malformed JSON or unreadable request body.
     *
     * <p>This method is triggered when the request body cannot be parsed
     * (e.g., malformed JSON, missing required fields in JSON).
     *
     * @param ex the message not readable exception
     * @return ResponseEntity with 400 status and error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        logger.warn("Malformed request body: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Malformed request body. Please check your JSON syntax."));
    }

    /**
     * Handles business logic errors.
     *
     * <p>This method is triggered when services throw {@link IllegalArgumentException}
     * for business rule violations such as:
     * <ul>
     *   <li>Duplicate entries</li>
     *   <li>Invalid credentials</li>
     *   <li>Invalid operations</li>
     *   <li>Access denied (ownership checks)</li>
     * </ul>
     *
     * @param ex the illegal argument exception
     * @return ResponseEntity with 400 status and error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Business logic error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * Handles resource not found errors.
     *
     * <p>This method is triggered when attempting to access a resource
     * that doesn't exist (e.g., calling {@code Optional.get()} on an empty Optional).
     *
     * @param ex the no such element exception
     * @return ResponseEntity with 404 status and error response
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElement(NoSuchElementException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Resource not found"));
    }

    /**
     * Handles Spring Security access denied errors.
     *
     * <p>This method is triggered when a user attempts to access a resource
     * they don't have permission for.
     *
     * @param ex the access denied exception
     * @return ResponseEntity with 403 status and error response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Access denied: " + ex.getMessage()));
    }

    /**
     * Handles all unexpected exceptions.
     *
     * <p>This is a catch-all handler for any exceptions not explicitly handled
     * by other methods. It returns a 500 Internal Server Error and logs the
     * full stack trace for debugging.
     *
     * <p><strong>Important:</strong> These errors indicate bugs or unexpected
     * conditions and should be investigated.
     *
     * @param ex the unexpected exception
     * @return ResponseEntity with 500 status and error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred. Please try again later."));
    }
}