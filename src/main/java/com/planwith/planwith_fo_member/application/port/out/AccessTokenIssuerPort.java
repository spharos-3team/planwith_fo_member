package com.planwith.planwith_fo_member.application.port.out;

import java.util.List;
import java.util.UUID;

public interface AccessTokenIssuerPort {

	record IssuedAccessToken(
			String accessToken,
			long expiresInSeconds,
			String sessionId,
			List<String> roles,
			List<String> scopes
	) {
	}

	IssuedAccessToken issue(UUID memberUuid, String sessionId);
}
