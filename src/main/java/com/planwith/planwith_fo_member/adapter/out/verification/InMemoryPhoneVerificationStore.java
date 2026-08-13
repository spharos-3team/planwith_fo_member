package com.planwith.planwith_fo_member.adapter.out.verification;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_member.application.port.out.PhoneVerificationStorePort;

@Component
public class InMemoryPhoneVerificationStore implements PhoneVerificationStorePort {

	private final Map<String, Instant> verifiedUntil = new ConcurrentHashMap<>();

	@Override
	public void markVerified(String phoneNumber, Instant verifiedUntilAt) {
		verifiedUntil.put(phoneNumber, verifiedUntilAt);
	}

	@Override
	public boolean isVerified(String phoneNumber) {
		Instant until = verifiedUntil.get(phoneNumber);
		if (until == null) {
			return false;
		}
		if (until.isBefore(Instant.now())) {
			verifiedUntil.remove(phoneNumber);
			return false;
		}
		return true;
	}

	@Override
	public Optional<String> findVerifiedPhone(String phoneNumber) {
		return isVerified(phoneNumber) ? Optional.of(phoneNumber) : Optional.empty();
	}

	@Override
	public void clear(String phoneNumber) {
		verifiedUntil.remove(phoneNumber);
	}

	public void clearAll() {
		verifiedUntil.clear();
	}
}
