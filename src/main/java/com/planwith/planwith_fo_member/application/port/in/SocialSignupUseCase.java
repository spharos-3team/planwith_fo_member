package com.planwith.planwith_fo_member.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_member.application.port.in.LocalLoginUseCase.AuthTokenResult;
import com.planwith.planwith_fo_member.domain.member.LoginType;

public interface SocialSignupUseCase {

	SocialSignupResult signup(LoginType provider, SocialSignupCommand command);

	record SocialSignupCommand(
			String authorizationCode,
			String redirectUri,
			String nickname,
			String profileImage,
			String profileIntro,
			String phoneNumber,
			List<AgreementItem> agreements
	) {
	}

	record AgreementItem(UUID termUuid, boolean agreed) {
	}

	record SocialSignupResult(
			UUID memberUuid,
			String email,
			String nickname,
			Instant createdAt,
			AuthTokenResult tokens
	) {
	}
}
