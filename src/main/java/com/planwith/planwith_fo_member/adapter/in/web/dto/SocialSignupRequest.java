package com.planwith.planwith_fo_member.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "소셜 회원가입 요청 (비밀번호 없음, 본인인증 전화번호 필수)")
public record SocialSignupRequest(
		@Schema(example = "stub:google-123:user@example.com", description = "스텁: stub:{socialId}:{email} / 실연동: OAuth authorization code")
		@NotBlank(message = "authorizationCode는 필수입니다.")
		String authorizationCode,

		@Schema(nullable = true, description = "OAuth redirect URI")
		String redirectUri,

		@Schema(example = "플랜위드")
		@NotBlank(message = "닉네임은 필수입니다.")
		@Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
		String nickname,

		@Schema(nullable = true)
		@Size(max = 1000, message = "프로필 이미지는 1000자 이하여야 합니다.")
		String profileImage,

		@Schema(nullable = true)
		@Size(max = 20, message = "프로필 소개는 20자 이하여야 합니다.")
		String profileIntro,

		@Schema(example = "01012345678")
		@NotBlank(message = "휴대폰 번호는 필수입니다.")
		@Size(max = 30, message = "휴대폰 번호는 30자 이하여야 합니다.")
		String phoneNumber,

		@Schema(example = "홍길동", description = "본인인증 실명과 일치해야 함")
		@NotBlank(message = "이름은 필수입니다.")
		@Size(max = 100, message = "이름은 100자 이하여야 합니다.")
		String name,

		@NotNull(message = "약관 동의 목록은 필수입니다.")
		@Valid
		List<AgreementRequest> agreements
) {
	public record AgreementRequest(
			@NotNull(message = "termUuid는 필수입니다.")
			UUID termUuid,

			@NotNull(message = "agreed는 필수입니다.")
			Boolean agreed
	) {
	}
}
