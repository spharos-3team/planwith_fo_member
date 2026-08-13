package com.planwith.planwith_fo_member.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenStorePort {

	record IssuedRefreshToken(
			String rawToken,
			UUID memberUuid,
			String sessionId,
			String familyId,
			Instant expiresAt
	) {
	}

	record StoredRefreshToken(
			String tokenHash,
			UUID memberUuid,
			String sessionId,
			String familyId,
			Instant expiresAt,
			boolean revoked
	) {
	}

	IssuedRefreshToken issue(UUID memberUuid, Instant expiresAt);

	IssuedRefreshToken rotate(String rawToken, Instant newExpiresAt);

	Optional<StoredRefreshToken> findActive(String rawToken);

	void revokeFamily(String familyId);

	void revokeByRawToken(String rawToken);

	void clearAll();
}
