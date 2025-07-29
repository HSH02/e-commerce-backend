package com.ecommerce.api.v1.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static com.ecommerce.global.utils.constants.ValidationConstants.*;


@Schema(description = "회원가입 요청 DTO")
public record RegisterRequest(
        @Schema(description = "사용자 닉네임", example = "hong123", minLength = NICKNAME_MIN_LENGTH, maxLength = NICKNAME_MAX_LENGTH)
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(
                min = NICKNAME_MIN_LENGTH, max = NICKNAME_MAX_LENGTH,
                message = "닉네임은 " + NICKNAME_MIN_LENGTH + "-" + NICKNAME_MAX_LENGTH + "자 사이여야 합니다"
        )
        String nickname,

        @Schema(description = "전화번호", example = "010-1234-5678", pattern = PHONE_NUMBER_PATTERN)
        @NotBlank(message = "전화번호는 필수입니다")
        @Pattern(regexp = PHONE_NUMBER_PATTERN, message = "올바른 전화번호 형식이 아닙니다")
        String phoneNumber,

        @Schema(description = "주소", example = "서울시 강남구 테헤란로 123", maxLength = ADDRESS_MAX_LENGTH)
        @NotBlank(message = "주소는 필수입니다")
        @Size(max = ADDRESS_MAX_LENGTH, message = "주소는 " + ADDRESS_MAX_LENGTH + "자 이하여야 합니다")
        String address,

        @Schema(description = "이메일", example = "hong@example.com", format = "email")
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @Schema(description = "비밀번호", example = "password123", minLength = PASSWORD_MIN_LENGTH, maxLength = PASSWORD_MAX_LENGTH)
        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH, message = "비밀번호는 " + PASSWORD_MIN_LENGTH + "-" + PASSWORD_MAX_LENGTH + "자 사이여야 합니다")
        String password,

        @Schema(description = "비밀번호 확인", example = "password123")
        @NotBlank(message = "비밀번호 확인은 필수입니다")
        String passwordConfirm
) {

}
