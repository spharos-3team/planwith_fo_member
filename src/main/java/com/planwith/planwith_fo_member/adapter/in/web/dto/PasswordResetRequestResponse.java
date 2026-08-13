package com.planwith.planwith_fo_member.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 재설정 인증번호 발송 결과")
public record PasswordResetRequestResponse(
		String email,
		long expiresInSeconds,
		String message
) {
}
