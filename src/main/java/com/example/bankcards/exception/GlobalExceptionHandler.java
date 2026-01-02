package com.example.bankcards.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String GENERIC_ERROR = "Произошла внутренняя ошибка сервера";
    private static final String VALIDATION_ERROR = "Ошибка валидации входных данных";
    private static final String AUTH_ERROR = "Неверные логин или пароль";
    private static final String ACCESS_DENIED = "Доступ запрещён. Требуется роль ADMIN.";
    private static final String RUNTIME_ERROR = "Ошибка обработки запроса";

    private static final String NULL_POINTER_ERROR = "Обнаружено пустое значение там, где оно недопустимо";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "error", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "error", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, "error", ACCESS_DENIED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "error", AUTH_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getBindingResult().getFieldErrors());

        Map<String, Object> response = new HashMap<>();
        response.put("error", VALIDATION_ERROR);
        List<Map<String, String>> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());
        response.put("details", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAmount(InvalidAmountException ex) {
        log.warn("Invalid amount: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "error", ex.getMessage());
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientFunds(InsufficientFundsException ex) {
        log.warn("Insufficient funds: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "error", ex.getMessage());
    }

    @ExceptionHandler(TransferNotAllowedException.class)
    public ResponseEntity<Map<String, Object>> handleTransferNotAllowed(TransferNotAllowedException ex) {
        log.warn("Transfer not allowed: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "error", ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", RUNTIME_ERROR);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointerException(NullPointerException ex) {
        log.error("Null pointer exception: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", NULL_POINTER_ERROR);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", GENERIC_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", GENERIC_ERROR);
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(
            HttpStatus status, String key, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put(key, message);
        return ResponseEntity.status(status).body(response);
    }

    private Map<String, String> formatFieldError(FieldError error) {
        Map<String, String> fieldError = new HashMap<>();
        fieldError.put("field", error.getField());
        fieldError.put("message", error.getDefaultMessage());
        return fieldError;
    }

}