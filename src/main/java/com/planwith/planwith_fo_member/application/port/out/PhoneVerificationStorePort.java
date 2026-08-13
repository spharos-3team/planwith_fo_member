package com.planwith.planwith_fo_member.application.port.out;

import java.time.Instant;
import java.util.Optional;

public interface PhoneVerificationStorePort {

	void markVerified(String phoneNumber, Instant verifiedUntil);

	boolean isVerified(String phoneNumber);

	Optional<String> findVerifiedPhone(String phoneNumber);

	void clear(String phoneNumber);
}
