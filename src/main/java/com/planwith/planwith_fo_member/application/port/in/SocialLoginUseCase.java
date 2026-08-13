package com.planwith.planwith_fo_member.application.port.in;

import com.planwith.planwith_fo_member.application.port.in.LocalLoginUseCase.AuthTokenResult;
import com.planwith.planwith_fo_member.domain.member.LoginType;

public interface SocialLoginUseCase {

	record SocialLoginCommand(String authorizationCode, String redirectUri) {
	}

	record SocialLoginResult(boolean isNewMember, AuthTokenResult tokens) {
	}

	SocialLoginResult login(LoginType provider, SocialLoginCommand command);
}
