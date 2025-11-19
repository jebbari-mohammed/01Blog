package com._Blog.mojebbari.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // This annotation makes this class listen to ALL Controllers
public class GlobalExceptionHandler {

    // 1. Handle Validation Errors (e.g., missing email, short password)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        // Extract the specific field errors (e.g., "email": "must be valid")
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 2. Handle Duplicate User Errors (Unique Constraint)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEntry(DataIntegrityViolationException ex) {
        Map<String, String> error = new HashMap<>();
        // This is a bit generic, but works for unique constraints like email/username
        error.put("error", "This username or email is already taken.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error); // Returns 409 Conflict
    }

    // 3. Handle Login Failures (Wrong Password/Email)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid email/username or password.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error); // Returns 401 Unauthorized
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, String> error = new HashMap<>();
        
        // We use ex.getMessage() to grab the message we set in the service!
        error.put("error", ex.getMessage()); 
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error); // Returns 403 Forbidden
    }
}