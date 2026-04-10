package com.rydvrse.common.error;

import com.rydvrse.common.api.ApiErrorBody;
import com.rydvrse.common.api.ApiErrorDetail;
import com.rydvrse.common.api.ApiErrorResponse;
import com.rydvrse.common.api.ApiMeta;
import com.rydvrse.common.util.RequestIdHolder;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception) {
        List<ApiErrorDetail> details = exception.details().entrySet().stream()
                .map(entry -> new ApiErrorDetail(entry.getKey(), entry.getValue()))
                .toList();
        return ResponseEntity.status(exception.status())
                .body(new ApiErrorResponse(
                        new ApiErrorBody(exception.errorCode().name(), exception.getMessage(), exception.type(), exception.retryable(), details),
                        new ApiMeta(RequestIdHolder.get(), OffsetDateTime.now(), "v1", null)
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<ApiErrorDetail> details = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .toList();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiErrorResponse(
                        new ApiErrorBody(ErrorCode.VALIDATION_ERROR.name(), "Validation failed", "validation_error", false, details),
                        ApiMeta.now(RequestIdHolder.get())
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException exception) {
        List<ApiErrorDetail> details = exception.getConstraintViolations().stream()
                .map(v -> new ApiErrorDetail(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiErrorResponse(
                        new ApiErrorBody(ErrorCode.VALIDATION_ERROR.name(), "Validation failed", "validation_error", false, details),
                        ApiMeta.now(RequestIdHolder.get())
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        new ApiErrorBody("INTERNAL_ERROR", exception.getMessage(), "server_error", false, List.of()),
                        ApiMeta.now(RequestIdHolder.get())
                ));
    }

    private ApiErrorDetail toDetail(FieldError fieldError) {
        return new ApiErrorDetail(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
