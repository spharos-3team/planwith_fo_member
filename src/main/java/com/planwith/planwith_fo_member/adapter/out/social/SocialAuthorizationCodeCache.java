package com.planwith.planwith_fo_member.adapter.out.social;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;

import com.planwith.planwith_fo_member.application.port.out.SocialOAuthClientPort.SocialUserProfile;
import com.planwith.planwith_fo_member.domain.member.LoginType;

/**
 * Google/Kakao/Naver authorization code is one-time. Login already exchanges it,
 * then signup would fail unless the profile is reused for the same code.
 */
final class SocialAuthorizationCodeCache {

	private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

	private final Clock clock;
	private final Duration ttl;
	private final ConcurrentHashMap<String, CacheEntry> entries = new ConcurrentHashMap<>();

	SocialAuthorizationCodeCache() {
		this(Clock.systemUTC(), DEFAULT_TTL);
	}

	SocialAuthorizationCodeCache(Clock clock, Duration ttl) {
		this.clock = clock;
		this.ttl = ttl;
	}

	SocialUserProfile get(LoginType provider, String authorizationCode) {
		String key = cacheKey(provider, authorizationCode);
		CacheEntry entry = entries.get(key);
		if (entry == null) {
			return null;
		}
		if (!clock.instant().isBefore(entry.expiresAt())) {
			entries.remove(key, entry);
			return null;
		}
		return entry.profile();
	}

	void put(LoginType provider, String authorizationCode, SocialUserProfile profile) {
		entries.put(
				cacheKey(provider, authorizationCode),
				new CacheEntry(profile, clock.instant().plus(ttl))
		);
	}

	private String cacheKey(LoginType provider, String authorizationCode) {
		return provider.name() + ":" + sha256(authorizationCode);
	}

	private String sha256(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is required", exception);
		}
	}

	private record CacheEntry(SocialUserProfile profile, Instant expiresAt) {
	}
}
