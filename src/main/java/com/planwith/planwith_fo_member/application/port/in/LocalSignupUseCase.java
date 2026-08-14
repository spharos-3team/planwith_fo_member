package com.planwith.planwith_fo_member.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LocalSignupUseCase {

	LocalSignupResult signup(LocalSignupCommand command);

	record LocalSignupCommand(
			String email,
			String password,
			String phoneNumber,
			String name,
			String nickname,
			String profileImage,
			String profileIntro,
			List<AgreementItem> agreements
	) {
	}

	record AgreementItem(UUID termUuid, boolean agreed) {
	}

	record LocalSignupResult(
			UUID memberUuid,
			String email,
			String nickname,
			Instant createdAt
	) {
	}
}
