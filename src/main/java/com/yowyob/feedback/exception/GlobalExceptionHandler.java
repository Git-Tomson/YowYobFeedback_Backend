package com.yowyob.feedback.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Catches and handles exceptions thrown by controllers.
 *
 * Returns standardized error responses to clients.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-12
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice // Dit à Spring d'écouter les exceptions de TOUS les contrôleurs
public class GlobalExceptionHandler {

    private static final String VALIDATION_ERROR_TYPE = "Validation error";
    private static final String INVALID_REQUEST_TYPE = "Invalid request";
    private static final String SERVER_ERROR_TYPE = "Server error";
    private static final String VALIDATION_ERROR_MESSAGE = "Provided data is invalid";
    private static final String SERVER_ERROR_MESSAGE = "An unexpected error occurred";

    /**
     * Handles validation exceptions from request body validation.
     *
     * @param exception the validation exception
     * @return Mono<ResponseEntity<ErrorResponse>> containing validation errors
     */
    @ExceptionHandler(WebExchangeBindException.class) //Cible
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(
            WebExchangeBindException exception) {
        log.error("Validation error: {}", exception.getMessage());

        Map<String, String> validation_errors = extractValidationErrors(exception);

        ErrorResponse error_response = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(VALIDATION_ERROR_TYPE)
                .message(VALIDATION_ERROR_MESSAGE)
                .validation_errors(validation_errors)
                .build();

        return Mono.just(ResponseEntity.badRequest().body(error_response));
    }

    /**
     * Handles IllegalArgumentException for business logic errors.
     *
     * Examples: user already exists, invalid credentials, missing required fields.
     *
     * @param exception the illegal argument exception
     * @return Mono<ResponseEntity<ErrorResponse>> containing error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException exception) {
        log.error("Business logic error: {}", exception.getMessage());

        ErrorResponse error_response = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(INVALID_REQUEST_TYPE)
                .message(exception.getMessage())
                .build();

        return Mono.just(ResponseEntity.badRequest().body(error_response));
    }

    /**
     * Handles all other unexpected exceptions.
     *
     * @param exception the unexpected exception
     * @return Mono<ResponseEntity<ErrorResponse>> containing generic error
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception exception) {
        log.error("Unexpected error occurred", exception);

        ErrorResponse error_response = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(SERVER_ERROR_TYPE)
                .message(SERVER_ERROR_MESSAGE)
                .build();

        return Mono.just(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error_response));
    }

    /**
     * Extracts field validation errors from exception.
     *
     * @param exception the binding exception
     * @return Map of field names to error messages
     */
    private Map<String, String> extractValidationErrors(WebExchangeBindException exception) {
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult().getAllErrors().forEach(error -> {
            String field_name = ((FieldError) error).getField();
            String error_message = error.getDefaultMessage();
            errors.put(field_name, error_message);
        });

        return errors;
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<Mono<ErrorResponse>> handleServerWebInputException(
            ServerWebInputException ex, ServerHttpRequest request) {

        String message = "Format de requête invalide";

        // On essaie d'extraire la cause précise (l'erreur d'Enum par exemple)
        if (ex.getCause() instanceof DecodingException decodingException) {
            message = "Valeur incorrecte fournie : " + decodingException.getMessage();
        }

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .path(request.getPath().value())
                .timestamp(OffsetDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Mono.just(error));
    }
}
