package com.planwith.planwith_fo_member.adapter.in.web.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 로그인 응답 (미가입이면 isNewMember=true, 토큰 없음)")
public record SocialLoginResponse(
		boolean isNewMember,
		String tokenType,
		String accessToken,
		Long accessTokenExpiresIn,
		TokenUser user
) {
	public record TokenUser(
			String userId,
			List<String> roles,
			List<String> scopes
	) {
	}
}
