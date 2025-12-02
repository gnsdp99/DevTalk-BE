package ktb3.full.community.presentation.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import ktb3.full.community.common.exception.ApiErrorCode;
import ktb3.full.community.common.exception.base.CustomException;
import ktb3.full.community.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(HttpServletRequest request, CustomException e) {
        logError(request, e);
        return ResponseEntity.status(e.getHttpStatus())
                .body(ApiResponse.error(e.getApiErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        logError(request, e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ApiErrorCode.INVALID_INPUT, e.getBindingResult().getAllErrors().getFirst().getDefaultMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpServletRequest request, HttpMessageNotReadableException e) {
        logError(request, e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ApiErrorCode.HTTP_MESSAGE_NOT_READABLE, e.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(HttpServletRequest request, MissingServletRequestParameterException e) {
        logError(request, e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ApiErrorCode.MISSING_SERVLET_REQUEST_PARAMETER, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(HttpServletRequest request, MethodArgumentTypeMismatchException e) {
        logError(request, e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ApiErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH, e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
        logError(request, e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ApiErrorCode.CONSTRAINT_VIOLATION, e.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(HttpServletRequest request, NoResourceFoundException e) {
        logError(request, e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ApiErrorCode.NO_RESOURCE_FOUND));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {
        logError(request, e);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(ApiErrorCode.METHOD_NOT_ALLOWED, e.getMessage()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException(HttpServletRequest request, HttpMediaTypeNotSupportedException e) {
        logError(request, e);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error(ApiErrorCode.UNSUPPORTED_MEDIA_TYPE, e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(HttpServletRequest request, AccessDeniedException e) {
        logError(request, e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ApiErrorCode.NO_PERMISSION, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(HttpServletRequest request, Exception e) {
        logError(request, e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ApiErrorCode.INTERNAL_SERVER_ERROR));
    }

    private void logError(HttpServletRequest request, Exception e) {
        log.error("Exception is occurred in path: {}", request.getRequestURI(), e);
    }
}
