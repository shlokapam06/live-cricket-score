package com.cricketscore.cricket_score.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException exception) {
		return response(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler({ IllegalArgumentException.class, DataIntegrityViolationException.class })
	public ResponseEntity<Map<String, Object>> handleBadRequest(Exception exception) {
		String message = exception instanceof IllegalArgumentException
				? exception.getMessage() : "A record with these details already exists";
		return response(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.findFirst().orElse("Request validation failed");
		return response(HttpStatus.BAD_REQUEST, message);
	}

	private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		return ResponseEntity.status(status).body(body);
	}
}