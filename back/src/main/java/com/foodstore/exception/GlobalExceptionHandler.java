package com.foodstore.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
                "resource_not_found",
                ex.getMessage(),
                404,
                request.getRequestURI()
        );
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
                "business_error",
                ex.getMessage(),
                400,
                request.getRequestURI()
        );
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fields.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ErrorResponse response = ErrorResponse.of(
                "validation_error",
                "Error de validación",
                400,
                request.getRequestURI(),
                fields
        );
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
                "access_denied",
                "Acceso denegado",
                403,
                request.getRequestURI()
        );
        return ResponseEntity.status(403).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "El cuerpo de la solicitud es inválido. Verificá que el JSON esté bien formado y los valores sean correctos.";
        if (ex.getMessage() != null && ex.getMessage().contains("Cannot deserialize")) {
            message = "Valor inválido para un campo enumerado. Valores aceptados: " + extractAcceptedValues(ex.getMessage());
        }
        ErrorResponse response = ErrorResponse.of(
                "bad_request",
                message,
                400,
                request.getRequestURI()
        );
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleMissingParam(
            Exception ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
                "bad_request",
                "Parámetro de solicitud inválido: " + ex.getMessage(),
                400,
                request.getRequestURI()
        );
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        ErrorResponse response = ErrorResponse.of(
                "internal_error",
                "Error interno del servidor",
                500,
                request.getRequestURI()
        );
        return ResponseEntity.status(500).body(response);
    }

    private String extractAcceptedValues(String errorMessage) {
        if (errorMessage == null) return "";
        int idx = errorMessage.indexOf("accepted for Enum class: [");
        if (idx != -1) {
            int start = idx + "accepted for Enum class: ".length();
            int end = errorMessage.indexOf("]", start);
            if (end != -1) {
                return errorMessage.substring(start, end);
            }
        }
        return "Revisá la documentación de la API";
    }
}
