package com.planwith.planwith_fo_member.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "이메일 인증번호 확인 요청")
public record EmailVerificationConfirmRequest(
		@NotBlank(message = "이메일은 필수입니다.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		String email,

		@NotBlank(message = "인증번호는 필수입니다.")
		@Size(min = 4, max = 10, message = "인증번호 형식이 올바르지 않습니다.")
		String code
) {
}
