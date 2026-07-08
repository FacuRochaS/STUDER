package facu.studer.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;

import java.util.HashMap;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        logger.warn("Validation error (400) at {}: {}", request.getRequestURI(), ex.getMessage());

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                        (existingMessage, newMessage) -> existingMessage + ", " + newMessage
                ));

        ApiError error = buildError(400, "Validation Error", "validation.error", null, request, fieldErrors);
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        logger.warn("Constraint violation (400) at {}: {}", request.getRequestURI(), ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String field = propertyPath.contains(".") ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1) : propertyPath;
            fieldErrors.put(field, violation.getMessage());
        });

        ApiError error = buildError(400, "Constraint Violation", "validation.constraint", null, request, fieldErrors);
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        logger.warn("Malformed JSON request (400) at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiError error = buildError(400, "Malformed Request", "error.malformed_json", null, request, null);
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        logger.warn("Business logic error (422) at {}: {}", request.getRequestURI(), ex.getMessageKey());

        ApiError error = buildError(422, "Business Error", ex.getMessageKey(), ex.getArgs(), request, null);
        return ResponseEntity.status(422).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        logger.warn("Illegal argument (400) at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiError error = buildError(400, "Bad Request", ex.getMessage(), null, request, null);
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOtherExceptions(
            Exception ex,
            HttpServletRequest request) {

        logger.error("🚨🚨🚨 An unexpected error occurred at {} 🚨🚨🚨", request.getRequestURI(), ex);

        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(500)
                .error("Internal Server Error")
                .code("error.internal")
                .message("An unexpected error occurred. Please check server logs for details.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(500).body(error);
    }

    // --- Métodos Auxiliares (DRY) ---


    private ApiError buildError(int status, String errorType, String messageKey, Object[] args, HttpServletRequest request, Map<String, String> fieldErrors) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(errorType)
                .code(messageKey)
                .message(resolveMessage(messageKey, args))
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();
    }

    private String resolveMessage(String messageKey, Object[] args) {
        if (messageKey == null) {
            return "An unknown error occurred.";
        }
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(messageKey, args, locale);
        } catch (Exception e) {
            return messageKey;
        }
    }
}