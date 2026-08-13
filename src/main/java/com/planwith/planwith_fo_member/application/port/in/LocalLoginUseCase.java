package com.planwith.planwith_fo_member.application.port.in;

import java.util.List;
import java.util.UUID;

public interface LocalLoginUseCase {

	record LocalLoginCommand(String email, String password) {
	}

	record AuthTokenResult(
			String accessToken,
			long accessTokenExpiresIn,
			String refreshToken,
			UUID memberUuid,
			List<String> roles,
			List<String> scopes
	) {
	}

	AuthTokenResult login(LocalLoginCommand command);
}
