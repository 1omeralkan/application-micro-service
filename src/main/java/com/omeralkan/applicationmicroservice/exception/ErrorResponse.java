package com.omeralkan.applicationmicroservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
}