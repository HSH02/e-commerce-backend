package com.ecommerce.global.utils;

import lombok.NoArgsConstructor;

import java.time.Duration;

@NoArgsConstructor
public final class DurationUtils {
    
    /**
     * 문자열을 Duration으로 파싱
     * 지원 형식: "30s", "30m", "7d", "1h", "604800000"
     */
    public static Duration parse(String duration) {
        if (duration == null || duration.trim().isEmpty()) {
            throw new IllegalArgumentException("Duration이 설정되지 않았습니다");
        }
        
        String trimmedDuration = duration.trim().toLowerCase();
        
        try {
            String unit = "";
            String number = trimmedDuration;
            
            if (trimmedDuration.length() > 1) {
                char lastChar = trimmedDuration.charAt(trimmedDuration.length() - 1);
                if (Character.isLetter(lastChar)) {
                    unit = String.valueOf(lastChar);
                    number = trimmedDuration.substring(0, trimmedDuration.length() - 1);
                }
            }
            
            long value = Long.parseLong(number);
            
            return switch (unit) {
                case "s" -> Duration.ofSeconds(value);
                case "m" -> Duration.ofMinutes(value);
                case "h" -> Duration.ofHours(value);
                case "d" -> Duration.ofDays(value);
                case "" -> Duration.ofMillis(value);  // 숫자만 있는 경우 밀리초로
                default -> throw new IllegalArgumentException("지원하지 않는 시간 단위: " + unit);
            };
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("잘못된 Duration 형식: " + duration, e);
        }
    }

    public static long parseToMillis(String duration) {
        return parse(duration).toMillis();
    }
} 