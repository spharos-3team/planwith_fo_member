package com.planwith.planwith_fo_member.application.port.in;

import java.util.UUID;

public interface GetMyProfileUseCase {

	ProfileResult get(UUID memberUuid);

	record ProfileResult(
			UUID memberUuid,
			String nickname,
			String profileImage,
			String profileIntro,
			String grade,
			boolean profileBadge,
			boolean profileSpecialBorder
	) {
	}
}
