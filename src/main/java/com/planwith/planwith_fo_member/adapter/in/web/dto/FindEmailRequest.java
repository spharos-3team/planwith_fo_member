package com.planwith.planwith_fo_member.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "아이디 찾기 요청 (본인인증 완료된 휴대폰 번호)")
public record FindEmailRequest(
		@Schema(example = "01012345678")
		@NotBlank(message = "휴대폰 번호는 필수입니다.")
		@Size(max = 30, message = "휴대폰 번호는 30자 이하여야 합니다.")
		String phoneNumber
) {
}
