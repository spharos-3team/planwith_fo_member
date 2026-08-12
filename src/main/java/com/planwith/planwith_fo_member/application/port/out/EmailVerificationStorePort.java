package com.planwith.planwith_fo_member.application.port.out;

import java.time.Instant;
import java.util.Optional;

public interface EmailVerificationStorePort {

	void saveCode(String email, String code, Instant expiresAt);

	Optional<StoredCode> findCode(String email);

	void markVerified(String email, Instant verifiedUntil);

	boolean isVerified(String email);

	void clear(String email);

	record StoredCode(String code, Instant expiresAt) {
	}
}
