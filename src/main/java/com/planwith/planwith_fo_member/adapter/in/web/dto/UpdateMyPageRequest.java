package com.planwith.planwith_fo_member.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 마이페이지 저장 요청. 보낸 필드만 반영한다.
 */
public record UpdateMyPageRequest(
		@Size(max = 30, message = "휴대폰 번호는 30자 이하여야 합니다.")
		String phoneNumber,

		@Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
		String nickname,

		@Size(max = 1000, message = "프로필 이미지는 1000자 이하여야 합니다.")
		String profileImage,

		@Size(max = 20, message = "프로필 소개는 20자 이하여야 합니다.")
		String profileIntro,

		@Valid
		List<AgreementItem> agreements,

		String currentPassword,

		@Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
		String newPassword
) {
	public record AgreementItem(
			@NotNull(message = "termUuid는 필수입니다.")
			UUID termUuid,

			@NotNull(message = "agreed는 필수입니다.")
			Boolean agreed
	) {
	}
}
