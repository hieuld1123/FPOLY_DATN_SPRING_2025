package com.example.datnsd26.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler({InvalidDataException.class})
    public ErrorResponse handleException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(BAD_REQUEST.value());
        errorResponse.setError("Invalid Data");
        errorResponse.setMessage(e.getMessage());
        log.error(e.getMessage());
        return errorResponse;
    }

    @Getter
    @Setter
    private class ErrorResponse {
        private int status;
        private String error;
        private String message;
    }
}
