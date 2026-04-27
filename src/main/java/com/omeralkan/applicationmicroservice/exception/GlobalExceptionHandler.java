package com.omeralkan.applicationmicroservice.exception;

import com.omeralkan.applicationmicroservice.service.ErrorMessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorMessageService errorMessageService;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        String language = request.getHeader("Accept-Language");
        String message = errorMessageService.getMessage(ex.getMessage(), language);

        log.error("Business exception: {} | Message: {}", ex.getMessage(), message);

        ErrorResponse response = new ErrorResponse(
                ex.getHttpStatus().value(),
                ex.getMessage(),
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        String language = request.getHeader("Accept-Language");
        String message = errorMessageService.getMessage("SYS-500", language);

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse response = new ErrorResponse(
                500,
                "SYS-500",
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.internalServerError().body(response);
    }
}