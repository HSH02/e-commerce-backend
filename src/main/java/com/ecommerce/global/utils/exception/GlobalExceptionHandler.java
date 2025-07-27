package com.ecommerce.global.utils.exception;

import com.ecommerce.global.utils.dto.RsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * ServiceException 처리 - 비즈니스 로직 관련 예외
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<RsData<Object>> handleServiceException(ServiceException e) {
        log.error("[ServiceException] 코드: {}, 메시지: {}", e.getCode(), e.getMessage());
        return ResponseEntity
                .status(e.getCode())
                .body(RsData.error(e.getCode(), e.getMessage()));
    }

    /**
     * 입력값 검증 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("[유효성 검증 실패] {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RsData.error(HttpStatus.BAD_REQUEST, errors, "입력값이 올바르지 않습니다."));
    }

    /**
     * 요청 파라미터 타입 불일치 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RsData<Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = e.getName() + " 파라미터의 타입이 잘못되었습니다. 기대하는 타입: " + e.getRequiredType().getSimpleName();
        log.warn("[파라미터 타입 불일치] {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RsData.error(HttpStatus.BAD_REQUEST, message));
    }

    /**
     * 필수 쿠키 누락 처리
     */
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<RsData<Object>> handleMissingCookieException(MissingRequestCookieException e) {
        String message = e.getCookieName() + " 쿠키가 필요합니다.";
        log.warn("[쿠키 누락] {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RsData.error(HttpStatus.BAD_REQUEST, message));
    }

    /**
     * 요청 URL에 해당하는 핸들러가 없을 때 처리
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<RsData<Object>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        String message = "요청하신 페이지를 찾을 수 없습니다. (" + e.getRequestURL() + ")";
        log.warn("[페이지 없음] {}", message);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(RsData.error(HttpStatus.NOT_FOUND, message));
    }

    /**
     * 접근 권한 오류 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RsData<Object>> handleAccessDeniedException(AccessDeniedException e) {
        String message = "해당 리소스에 접근할 권한이 없습니다.";
        log.warn("[접근 권한 오류] {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(RsData.error(HttpStatus.FORBIDDEN, message));
    }

    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RsData<Object>> handleException(Exception e) {
        log.error("[서버 오류] 메시지: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RsData.error(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."));
    }
}