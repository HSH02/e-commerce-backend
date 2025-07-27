package com.ecommerce.global.utils.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class RsData<T> {
    private final Integer code;
    private final boolean success;
    private final T data;
    private final String message;
    private final String timestamp;

    public RsData(Integer code, boolean success, T data, String message) {
        this.code = code;
        this.success = success;
        this.data = data;
        this.message = message;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public static <T> RsData<T> success(HttpStatus resultCode) {
        return new RsData<>(resultCode.value(), true, null, null);
    }

    public static <T> RsData<T> success(HttpStatus resultCode, T data) {
        return new RsData<>(resultCode.value(), true, data, null);
    }

    public static <T> RsData<T> success(HttpStatus resultCode, T data, String message) {
        return new RsData<>(resultCode.value(), true, data, message);
    }

    public static <T> RsData<T> error(HttpStatus resultCode, String message) {
        return new RsData<>(resultCode.value(), false, null, message);
    }

    public static <T> RsData<T> error(HttpStatus resultCode, T data, String message) {
        return new RsData<>(resultCode.value(), false, data, message);
    }

}