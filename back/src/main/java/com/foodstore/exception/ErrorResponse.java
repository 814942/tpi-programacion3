package com.foodstore.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
    String error,
    String message,
    int status,
    String timestamp,
    String path,
    Map<String, String> fields
) {
    public static ErrorResponse of(String error, String message, int status, String path) {
        return new ErrorResponse(error, message, status, LocalDateTime.now().toString(), path, null);
    }

    public static ErrorResponse of(String error, String message, int status, String path, Map<String, String> fields) {
        return new ErrorResponse(error, message, status, LocalDateTime.now().toString(), path, fields);
    }
}
