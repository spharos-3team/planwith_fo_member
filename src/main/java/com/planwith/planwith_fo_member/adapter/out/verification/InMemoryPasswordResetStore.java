package com.planwith.planwith_fo_member.adapter.out.verification;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_member.application.port.out.PasswordResetStorePort;

@Component
public class InMemoryPasswordResetStore implements PasswordResetStorePort {

	private final Map<String, StoredCode> codes = new ConcurrentHashMap<>();

	@Override
	public void saveCode(String email, String code, Instant expiresAt) {
		codes.put(email, new StoredCode(code, expiresAt));
	}

	@Override
	public Optional<StoredCode> findCode(String email) {
		return Optional.ofNullable(codes.get(email));
	}

	@Override
	public void clear(String email) {
		codes.remove(email);
	}

	public void clearAll() {
		codes.clear();
	}
}
