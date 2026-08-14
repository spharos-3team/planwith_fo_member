package com.planwith.planwith_fo_member.application.port.out;

import java.time.Instant;
import java.util.Optional;

public interface PhoneVerificationStorePort {

	void markVerified(String phoneNumber, String name, Instant verifiedUntil);

	boolean isVerified(String phoneNumber);

	Optional<VerifiedPhone> findVerified(String phoneNumber);

	void clear(String phoneNumber);

	record VerifiedPhone(String phoneNumber, String name, Instant verifiedUntil) {
	}
}
