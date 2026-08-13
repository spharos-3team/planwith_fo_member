package com.planwith.planwith_fo_member.adapter.out.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.out.RefreshTokenStorePort;

/**
 * 로컬/테스트용 인메모리 Refresh 저장소.
 * 운영에서는 Redis 해시 저장소로 교체한다.
 */
@Component
public class InMemoryRefreshTokenStore implements RefreshTokenStorePort {

	private final SecureRandom secureRandom = new SecureRandom();
	private final Map<String, StoredRefreshToken> byHash = new ConcurrentHashMap<>();
	private final Map<String, Set<String>> familyHashes = new ConcurrentHashMap<>();

	@Override
	public IssuedRefreshToken issue(UUID memberUuid, Instant expiresAt) {
		String sessionId = UUID.randomUUID().toString();
		String familyId = UUID.randomUUID().toString();
		return persistNew(memberUuid, sessionId, familyId, expiresAt);
	}

	@Override
	public IssuedRefreshToken rotate(String rawToken, Instant newExpiresAt) {
		String presentedHash = hash(rawToken);
		StoredRefreshToken current = byHash.get(presentedHash);
		if (current == null) {
			throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
		}
		if (current.revoked()) {
			revokeFamily(current.familyId());
			throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "재사용된 Refresh Token입니다.");
		}
		if (current.expiresAt().isBefore(Instant.now())) {
			revokeFamily(current.familyId());
			throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "만료된 Refresh Token입니다.");
		}

		byHash.put(presentedHash, new StoredRefreshToken(
				current.tokenHash(),
				current.memberUuid(),
				current.sessionId(),
				current.familyId(),
				current.expiresAt(),
				true
		));

		return persistNew(current.memberUuid(), current.sessionId(), current.familyId(), newExpiresAt);
	}

	@Override
	public Optional<StoredRefreshToken> findActive(String rawToken) {
		StoredRefreshToken stored = byHash.get(hash(rawToken));
		if (stored == null || stored.revoked() || stored.expiresAt().isBefore(Instant.now())) {
			return Optional.empty();
		}
		return Optional.of(stored);
	}

	@Override
	public void revokeFamily(String familyId) {
		Set<String> hashes = familyHashes.remove(familyId);
		if (hashes == null) {
			return;
		}
		for (String tokenHash : hashes) {
			StoredRefreshToken stored = byHash.get(tokenHash);
			if (stored != null) {
				byHash.put(tokenHash, new StoredRefreshToken(
						stored.tokenHash(),
						stored.memberUuid(),
						stored.sessionId(),
						stored.familyId(),
						stored.expiresAt(),
						true
				));
			}
		}
	}

	@Override
	public void revokeByRawToken(String rawToken) {
		findActive(rawToken).ifPresent(stored -> revokeFamily(stored.familyId()));
	}

	@Override
	public void revokeAllForMember(UUID memberUuid) {
		byHash.values().stream()
				.filter(stored -> stored.memberUuid().equals(memberUuid) && !stored.revoked())
				.map(StoredRefreshToken::familyId)
				.distinct()
				.forEach(this::revokeFamily);
	}

	@Override
	public void clearAll() {
		byHash.clear();
		familyHashes.clear();
	}

	private IssuedRefreshToken persistNew(UUID memberUuid, String sessionId, String familyId, Instant expiresAt) {
		String rawToken = generateRawToken();
		String tokenHash = hash(rawToken);
		StoredRefreshToken stored = new StoredRefreshToken(tokenHash, memberUuid, sessionId, familyId, expiresAt, false);
		byHash.put(tokenHash, stored);
		familyHashes.computeIfAbsent(familyId, ignored -> ConcurrentHashMap.newKeySet()).add(tokenHash);
		return new IssuedRefreshToken(rawToken, memberUuid, sessionId, familyId, expiresAt);
	}

	private String generateRawToken() {
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String hash(String rawToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashed);
		}
		catch (Exception exception) {
			throw new IllegalStateException("Refresh Token 해시 실패", exception);
		}
	}
}
