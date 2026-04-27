package com.omeralkan.applicationmicroservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.error("Business exception: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                ex.getHttpStatus().value(),
                ex.getMessage(),
                ex.getHttpStatus().getReasonPhrase(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse response = new ErrorResponse(
                500,
                "INTERNAL_ERROR",
                "Beklenmeyen bir hata oluştu.",
                LocalDateTime.now()
        );

        return ResponseEntity.internalServerError().body(response);
    }
}