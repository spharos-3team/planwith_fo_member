package com.planwith.planwith_fo_member.application.port.out;

import com.planwith.planwith_fo_member.domain.member.LoginType;

public interface SocialOAuthClientPort {

	SocialUserProfile fetchUser(LoginType provider, String authorizationCode, String redirectUri);

	record SocialUserProfile(
			String socialId,
			String email,
			String profileImageUrl,
			String displayName
	) {
	}
}
