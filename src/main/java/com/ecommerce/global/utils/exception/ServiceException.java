package com.ecommerce.global.utils.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceException extends RuntimeException {
    private final HttpStatus code;
    private final String message;

    public ServiceException(HttpStatus code, String message) {
        super(code + " : " + message);
        this.code = code;
        this.message = message;
    }

    public ServiceException(HttpStatus code, String message, Throwable cause) {
        super(code + " : " + message, cause);
        this.code = code;
        this.message = message;
    }

    public ServiceException(ErrorCode errorCode) {
        super(errorCode.getHttpStatus() + " : " + errorCode.getMessage());
        this.code = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }

    public ServiceException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getHttpStatus() + " : " + errorCode.getMessage(), cause);
        this.code = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}