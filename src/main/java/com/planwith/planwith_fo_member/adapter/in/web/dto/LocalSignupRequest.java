package com.planwith.planwith_fo_member.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "로컬 회원가입 요청")
public record LocalSignupRequest(
		@Schema(example = "user@example.com")
		@NotBlank(message = "이메일은 필수입니다.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		String email,

		@Schema(example = "Password1!")
		@NotBlank(message = "비밀번호는 필수입니다.")
		@Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
		String password,

		@Schema(example = "01012345678")
		@NotBlank(message = "휴대폰 번호는 필수입니다.")
		@Size(max = 30, message = "휴대폰 번호는 30자 이하여야 합니다.")
		String phoneNumber,

		@Schema(example = "플랜위드")
		@NotBlank(message = "닉네임은 필수입니다.")
		@Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
		String nickname,

		@Schema(nullable = true)
		@Size(max = 1000, message = "프로필 이미지는 1000자 이하여야 합니다.")
		String profileImage,

		@Schema(nullable = true)
		@Size(max = 100, message = "프로필 소개는 100자 이하여야 합니다.")
		String profileIntro,

		@NotNull(message = "약관 동의 목록은 필수입니다.")
		@Valid
		List<AgreementRequest> agreements
) {
	@Schema(description = "약관 동의 항목")
	public record AgreementRequest(
			@NotNull(message = "termUuid는 필수입니다.")
			UUID termUuid,

			@NotNull(message = "agreed는 필수입니다.")
			Boolean agreed
	) {
	}
}
