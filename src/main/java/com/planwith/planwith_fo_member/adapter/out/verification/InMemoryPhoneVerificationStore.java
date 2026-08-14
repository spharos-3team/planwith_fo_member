package com.planwith.planwith_fo_member.adapter.out.verification;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_member.application.port.out.PhoneVerificationStorePort;

@Component
public class InMemoryPhoneVerificationStore implements PhoneVerificationStorePort {

	private final Map<String, VerifiedPhone> verified = new ConcurrentHashMap<>();

	@Override
	public void markVerified(String phoneNumber, String name, Instant verifiedUntilAt) {
		verified.put(phoneNumber, new VerifiedPhone(phoneNumber, name, verifiedUntilAt));
	}

	@Override
	public boolean isVerified(String phoneNumber) {
		return findVerified(phoneNumber).isPresent();
	}

	@Override
	public Optional<VerifiedPhone> findVerified(String phoneNumber) {
		VerifiedPhone entry = verified.get(phoneNumber);
		if (entry == null) {
			return Optional.empty();
		}
		if (entry.verifiedUntil().isBefore(Instant.now())) {
			verified.remove(phoneNumber);
			return Optional.empty();
		}
		return Optional.of(entry);
	}

	@Override
	public void clear(String phoneNumber) {
		verified.remove(phoneNumber);
	}

	public void clearAll() {
		verified.clear();
	}
}
