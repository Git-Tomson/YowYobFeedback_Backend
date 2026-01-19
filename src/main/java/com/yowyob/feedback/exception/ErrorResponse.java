package com.yowyob.feedback.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Standard error response format for the application.
 * Used by the global exception handler to return consistent error messages.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-12
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private OffsetDateTime timestamp;

    private Integer status;

    private String error;

    private String path;

    private String message;

    private Map<String, String> validation_errors;
}
