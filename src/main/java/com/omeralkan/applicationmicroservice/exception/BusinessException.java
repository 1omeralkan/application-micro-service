package com.omeralkan.applicationmicroservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus httpStatus;

    public BusinessException(String errorCode, HttpStatus httpStatus) {
        super(errorCode);
        this.httpStatus = httpStatus;
    }
}