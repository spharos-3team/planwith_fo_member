package com.planwith.planwith_fo_member.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "소셜 로그인 요청")
public record SocialLoginRequest(
		@Schema(example = "stub:google-123:user@example.com")
		@NotBlank(message = "authorizationCode는 필수입니다.")
		String authorizationCode,

		@Schema(nullable = true)
		String redirectUri,

		@Schema(nullable = true, description = "OAuth authorize 때 사용한 state. 네이버 토큰 교환에 필요")
		String state
) {
}
