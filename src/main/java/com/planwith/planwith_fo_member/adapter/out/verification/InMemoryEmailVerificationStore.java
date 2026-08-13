package com.planwith.planwith_fo_member.adapter.out.verification;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_member.application.port.out.EmailVerificationStorePort;

@Component
public class InMemoryEmailVerificationStore implements EmailVerificationStorePort {

	private final Map<String, StoredCode> codes = new ConcurrentHashMap<>();
	private final Map<String, Instant> verifiedUntil = new ConcurrentHashMap<>();

	@Override
	public void saveCode(String email, String code, Instant expiresAt) {
		codes.put(email, new StoredCode(code, expiresAt));
		verifiedUntil.remove(email);
	}

	@Override
	public Optional<StoredCode> findCode(String email) {
		return Optional.ofNullable(codes.get(email));
	}

	@Override
	public void markVerified(String email, Instant verifiedUntilAt) {
		codes.remove(email);
		verifiedUntil.put(email, verifiedUntilAt);
	}

	@Override
	public boolean isVerified(String email) {
		Instant until = verifiedUntil.get(email);
		if (until == null) {
			return false;
		}
		if (until.isBefore(Instant.now())) {
			verifiedUntil.remove(email);
			return false;
		}
		return true;
	}

	@Override
	public void clear(String email) {
		codes.remove(email);
		verifiedUntil.remove(email);
	}

	public void clearAll() {
		codes.clear();
		verifiedUntil.clear();
	}
}
