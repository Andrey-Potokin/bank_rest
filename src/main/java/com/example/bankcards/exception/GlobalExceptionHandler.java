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

/**
 * Глобальный обработчик исключений для приложения банковских карт.
 * <p>
 * Класс помечен аннотацией {@link RestControllerAdvice}, что позволяет перехватывать исключения,
 * выбрасываемые в контроллерах, и возвращать структурированные JSON-ответы с информацией об ошибке.
 * <p>
 * Логирует все исключения и возвращает клиенту понятные сообщения с соответствующим HTTP-статусом.
 * Предотвращает утечку внутренних деталей ошибок (стеков вызовов) в ответ клиенту.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String GENERIC_ERROR = "Произошла внутренняя ошибка сервера";
    private static final String VALIDATION_ERROR = "Ошибка валидации входных данных";
    private static final String AUTH_ERROR = "Неверные логин или пароль";
    private static final String ACCESS_DENIED = "Доступ запрещён. Требуется роль ADMIN.";
    private static final String RUNTIME_ERROR = "Ошибка обработки запроса";
    private static final String NULL_POINTER_ERROR = "Обнаружено пустое значение там, где оно недопустимо";

    /**
     * Обрабатывает исключение, возникающее при отсутствии сущности (например, пользователь или карта не найдены).
     * <p>
     * Логирует предупреждение и возвращает HTTP-статус 404 (Not Found).
     *
     * @param ex исключение типа {@link NotFoundException}
     * @return ответ с сообщением об ошибке и статусом 404
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "error", ex.getMessage());
    }

    /**
     * Обрабатывает исключение, возникающее при передаче недопустимого аргумента (например, пустой ID).
     * <p>
     * Логирует предупреждение и возвращает HTTP-статус 400 (Bad Request).
     *
     * @param ex исключение типа {@link IllegalArgumentException}
     * @return ответ с сообщением об ошибке и статусом 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "error", ex.getMessage());
    }

    /**
     * Обрабатывает исключение, возникающее при попытке доступа к ресурсу без достаточных прав.
     * <p>
     * Логирует предупреждение и возвращает HTTP-статус 403 (Forbidden).
     *
     * @param ex исключение типа {@link AccessDeniedException}
     * @return ответ с сообщением об ошибке и статусом 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, "error", ACCESS_DENIED);
    }

    /**
     * Обрабатывает исключение, возникающее при вводе неверного логина или пароля.
     * <p>
     * Логирует предупреждение и возвращает HTTP-статус 401 (Unauthorized).
     *
     * @param ex исключение типа {@link BadCredentialsException}
     * @return ответ с сообщением об ошибке и статусом 401
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "error", AUTH_ERROR);
    }

    /**
     * Обрабатывает исключение, возникающее при ошибках валидации DTO (например, неверный формат данных).
     * <p>
     * Возвращает список всех полей, не прошедших валидацию, с описанием ошибок.
     * Использует HTTP-статус 400 (Bad Request).
     *
     * @param ex исключение типа {@link MethodArgumentNotValidException}
     * @return ответ с обобщённым сообщением и детализацией по каждому полю
     */
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

    /**
     * Обрабатывает исключение, возникающее при передаче некорректной суммы (например, отрицательной).
     * <p>
     * Логирует предупреждение и возвращает HTTP-статус 400 (Bad Request).
     *
     * @param ex исключение типа {@link InvalidAmountException}
     * @return ответ с сообщением об ошибке и статусом 400
     */
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAmount(InvalidAmountException ex) {
        log.warn("Invalid amount: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "error", ex.getMessage());
    }

    /**
     * Обрабатывает исключение, возникающее при недостатке средств на карте для перевода.
     * <p>
     * Логирует предупреждение и возвращает HTTP-статус 400 (Bad Request).
     *
     * @param ex исключение типа {@link InsufficientFundsException}
     * @return ответ с сообщением об ошибке и статусом 400
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientFunds(InsufficientFundsException ex) {
        log.warn("Insufficient funds: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "error", ex.getMessage());
    }

    /**
     * Обрабатывает исключение, возникающее при попытке недопустимого перевода (например, с заблокированной карты).
     * <p>
     * Логирует предупреждение и возвращает HTTP-статус 400 (Bad Request).
     *
     * @param ex исключение типа {@link TransferNotAllowedException}
     * @return ответ с сообщением об ошибке и статусом 400
     */
    @ExceptionHandler(TransferNotAllowedException.class)
    public ResponseEntity<Map<String, Object>> handleTransferNotAllowed(TransferNotAllowedException ex) {
        log.warn("Transfer not allowed: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "error", ex.getMessage());
    }

    /**
     * Обрабатывает исключение, возникающее при проверке недействительного или просроченного JWT-токена.
     * <p>
     * Логирует предупреждение и возвращает HTTP-статус 401 (Unauthorized).
     *
     * @param ex исключение типа {@link JwtValidationException}
     * @return ответ с сообщением об ошибке и статусом 401
     */
    @ExceptionHandler(JwtValidationException.class)
    public ResponseEntity<Map<String, Object>> handleJwtValidationException(JwtValidationException ex) {
        log.warn("JWT validation failed: {}", ex.getMessage());
        return createErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "error",
                "Недействительный или просроченный токен JWT"
        );
    }

    /**
     * Обрабатывает общие исключения времени выполнения.
     * <p>
     * Логирует ошибку с полным стеком вызовов и возвращает HTTP-статус 500 (Internal Server Error).
     *
     * @param ex исключение типа {@link RuntimeException}
     * @return ответ с обобщённым сообщением об ошибке
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", RUNTIME_ERROR);
    }

    /**
     * Обрабатывает исключение, возникающее при попытке доступа к методу или полю у нулевого объекта.
     * <p>
     * Логирует ошибку с полным стеком вызовов и возвращает HTTP-статус 500 (Internal Server Error).
     * Рекомендуется устранить подобные ошибки на этапе разработки.
     *
     * @param ex исключение типа {@link NullPointerException}
     * @return ответ с обобщённым сообщением об ошибке
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointerException(NullPointerException ex) {
        log.error("Null pointer exception: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", NULL_POINTER_ERROR);
    }

    /**
     * Обрабатывает исключение, возникающее при вызове метода в неподходящем состоянии объекта.
     * <p>
     * Например, попытка активации уже активной карты. Логирует ошибку и возвращает HTTP-статус 500.
     *
     * @param ex исключение типа {@link IllegalStateException}
     * @return ответ с обобщённым сообщением об ошибке
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", GENERIC_ERROR);
    }

    /**
     * Обрабатывает все остальные непойманные исключения.
     * <p>
     * Является "резервным" обработчиком. Логирует ошибку с полным стеком и возвращает
     * обобщённое сообщение, чтобы не раскрывать внутренние детали клиенту.
     *
     * @param ex любое исключение типа {@link Exception}
     * @return ответ с обобщённым сообщением и статусом 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", GENERIC_ERROR);
    }

    /**
     * Вспомогательный метод для формирования стандартного ответа с ошибкой.
     * <p>
     * Создаёт карту (map), содержащую ключ и сообщение об ошибке, и упаковывает её в ResponseEntity.
     *
     * @param status HTTP-статус для ответа
     * @param key ключ ошибки (например, "error")
     * @param message текстовое сообщение об ошибке
     * @return ResponseEntity с телом ошибки и указанным статусом
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(
            HttpStatus status, String key, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put(key, message);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Вспомогательный метод для форматирования ошибки валидации поля.
     * <p>
     * Преобразует объект {@link FieldError} в карту с полями "field" и "message".
     * Используется при обработке {@link MethodArgumentNotValidException}.
     *
     * @param error объект ошибки валидации поля
     * @return карта с именем поля и сообщением об ошибке
     */
    private Map<String, String> formatFieldError(FieldError error) {
        Map<String, String> fieldError = new HashMap<>();
        fieldError.put("field", error.getField());
        fieldError.put("message", error.getDefaultMessage());
        return fieldError;
    }
}