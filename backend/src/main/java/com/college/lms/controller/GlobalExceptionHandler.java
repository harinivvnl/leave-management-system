package com.college.lms.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", exception.getMessage()));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException exception) {
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("message", "Invalid username or password"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
    FieldError fieldError = exception.getBindingResult().getFieldError();
    String message = fieldError != null
        ? fieldError.getField() + " is required"
        : "Validation failed";
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", message));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException exception) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", "Invalid request format. Check date/time values."));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException exception) {
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(Map.of("message", exception.getMessage()));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<Map<String, String>> handleMaxUploadSize(MaxUploadSizeExceededException exception) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", "Attachment is too large. Please upload a file smaller than 10MB."));
  }

  @ExceptionHandler(MultipartException.class)
  public ResponseEntity<Map<String, String>> handleMultipart(MultipartException exception) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", "Invalid attachment upload. Please reselect the file and try again."));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException exception) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", "Unable to save attachment. Please try a smaller image/file."));
  }
}