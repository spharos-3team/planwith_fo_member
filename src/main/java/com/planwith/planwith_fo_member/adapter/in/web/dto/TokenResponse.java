package com.planwith.planwith_fo_member.adapter.in.web.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 응답 (Refresh Token은 Cookie)")
public record TokenResponse(
		@Schema(example = "Bearer")
		String tokenType,
		String accessToken,
		long accessTokenExpiresIn,
		TokenUser user
) {
	public record TokenUser(
			String userId,
			List<String> roles,
			List<String> scopes
	) {
	}
}
