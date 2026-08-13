package com.planwith.planwith_fo_member.adapter.in.web.dto;

import com.planwith.planwith_fo_member.domain.member.LoginType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "아이디 찾기 응답")
public record FindEmailResponse(
		String email,
		String maskedEmail,
		@Schema(example = "LOCAL", description = "LOCAL | GOOGLE | NAVER | KAKAO")
		LoginType loginType
) {
}
