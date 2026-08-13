package com.planwith.planwith_fo_member.adapter.in.web.dto;

import java.util.UUID;

public record MemberProfileResponse(
		UUID memberUuid,
		String nickname,
		String profileImage,
		String profileIntro,
		String grade
) {
}
