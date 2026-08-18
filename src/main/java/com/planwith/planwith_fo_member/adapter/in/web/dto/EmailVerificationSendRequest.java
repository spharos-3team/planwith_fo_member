package com.planwith.planwith_fo_member.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 인증번호 발송 요청. 응답에 코드는 없고, 메일(또는 스텁 로그)로 전달된다.")
public record EmailVerificationSendRequest(
		@Schema(example = "user@example.com")
		@NotBlank(message = "이메일은 필수입니다.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		String email
) {
}
