package com.planwith.planwith_fo_member.adapter.in.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.planwith.planwith_fo_member.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return ResponseEntity.status(errorCode.status()).body(
				ApiResponse.failure(errorCode.code(), exception.getMessage(), Map.of())
		);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
				fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage())
		);
		return ResponseEntity.badRequest().body(
				ApiResponse.failure(ErrorCode.INVALID_REQUEST.code(), ErrorCode.INVALID_REQUEST.message(), fieldErrors)
		);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getConstraintViolations().forEach(violation -> {
			String path = violation.getPropertyPath().toString();
			String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
			fieldErrors.putIfAbsent(field, violation.getMessage());
		});
		return ResponseEntity.badRequest().body(
				ApiResponse.failure(ErrorCode.INVALID_REQUEST.code(), ErrorCode.INVALID_REQUEST.message(), fieldErrors)
		);
	}

	@ExceptionHandler({
			MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class,
			HttpMessageNotReadableException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
		return ResponseEntity.badRequest().body(
				ApiResponse.failure(ErrorCode.INVALID_REQUEST.code(), ErrorCode.INVALID_REQUEST.message(), Map.of())
		);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
		log.error("Unexpected request processing failure", exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
				ApiResponse.failure(
						ErrorCode.INTERNAL_SERVER_ERROR.code(),
						ErrorCode.INTERNAL_SERVER_ERROR.message(),
						Map.of()
				)
		);
	}
}
