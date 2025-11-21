package ktb3.full.community.presentation.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import ktb3.full.community.common.exception.ApiErrorCode;
import ktb3.full.community.dto.response.ApiErrorResponse;
import ktb3.full.community.common.exception.base.CustomException;
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
    public ResponseEntity<ApiErrorResponse> handleCustomException(HttpServletRequest request, CustomException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(ApiErrorResponse.of(e.getApiErrorCode(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.ofFieldError(ApiErrorCode.INVALID_INPUT, e.getBindingResult(), request.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(HttpServletRequest request, HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.ofDetail(ApiErrorCode.HTTP_MESSAGE_NOT_READABLE, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameterException(HttpServletRequest request, MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.ofDetail(ApiErrorCode.MISSING_SERVLET_REQUEST_PARAMETER, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(HttpServletRequest request, MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.ofDetail(ApiErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.ofDetail(ApiErrorCode.CONSTRAINT_VIOLATION, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(HttpServletRequest request, NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(ApiErrorCode.NO_RESOURCE_FOUND, request.getRequestURI()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiErrorResponse.ofDetail(ApiErrorCode.METHOD_NOT_ALLOWED, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMediaTypeNotSupportedException(HttpServletRequest request, HttpMediaTypeNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiErrorResponse.ofDetail(ApiErrorCode.UNSUPPORTED_MEDIA_TYPE, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(HttpServletRequest request, AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.ofDetail(ApiErrorCode.NO_PERMISSION, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(HttpServletRequest request, Exception e) {
        log.error("Exception is occurred in path: {}", request.getRequestURI(), e);
        return ResponseEntity.internalServerError()
                .body(ApiErrorResponse.of(ApiErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI()));
    }
}
