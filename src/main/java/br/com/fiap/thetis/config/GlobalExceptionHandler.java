package br.com.fiap.thetis.config;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import br.com.fiap.thetis.exception.BusinessException;
import br.com.fiap.thetis.exception.ErrorResponse;
import br.com.fiap.thetis.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldError> fields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());
        ErrorResponse body = new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
                "Validation Failed", "Requisição inválida", req.getRequestURI(), fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldError> fields = ex.getConstraintViolations().stream()
                .map(cv -> new ErrorResponse.FieldError(cv.getPropertyPath().toString(), cv.getMessage()))
                .collect(Collectors.toList());
        ErrorResponse body = new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
                "Constraint Violation", ex.getMessage(), req.getRequestURI(), fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(Instant.now(), HttpStatus.NOT_FOUND.value(),
                "Not Found", ex.getMessage(), req.getRequestURI(), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(Instant.now(), HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Business Rule", ex.getMessage(), req.getRequestURI(), List.of());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
                "Bad Request", ex.getMessage(), req.getRequestURI(), List.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Error", "Ocorreu um erro inesperado", req.getRequestURI(), List.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
