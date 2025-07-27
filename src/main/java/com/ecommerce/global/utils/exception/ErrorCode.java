package com.ecommerce.global.utils.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@Slf4j
public enum ErrorCode {
    // 인증, 인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INVALID_OAUTH_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 OAUTH_CODE 입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    MISSING_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "Access Token이 존재하지 않습니다."),

    // 유저
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이메일을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용중인 이름입니다."),
    DUPLICATE_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "이미 사용중인 전화번호입니다."),

    // 회원가입,
    PASSWORD_NOT_EQUAL(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

    // 로그인
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),

    // 인증
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 Access Token 입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token 입니다."),

    // REIDS
    REDIS_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 작업 중 오류가 발생했습니다."),
    INVALID_REFRESH_REDIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Refresh Token이 유효하지 않거나 탈취되었을 수 있습니다"),

    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다.");;

    private final HttpStatus httpStatus;
    private final String message;
} 