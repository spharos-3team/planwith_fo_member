package com.planwith.planwith_fo_member.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "본인인증 완료 확인 요청")
public record PhoneVerificationConfirmRequest(
		@NotBlank(message = "identityVerificationId는 필수입니다.")
		String identityVerificationId
) {
}
