package com.planwith.planwith_fo_member.adapter.out.social;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_member.application.port.out.SocialOAuthClientPort.SocialUserProfile;
import com.planwith.planwith_fo_member.domain.member.LoginType;

class SocialAuthorizationCodeCacheTest {

	@Test
	void returnsNullForUnknownCode() {
		SocialAuthorizationCodeCache cache = new SocialAuthorizationCodeCache();

		assertThat(cache.get(LoginType.GOOGLE, "missing")).isNull();
	}

	@Test
	void returnsCachedProfileForSameProviderAndCode() {
		SocialAuthorizationCodeCache cache = new SocialAuthorizationCodeCache();
		SocialUserProfile profile = new SocialUserProfile("sub-1", "user@example.com", null, "사용자");

		cache.put(LoginType.GOOGLE, "code-1", profile);

		assertThat(cache.get(LoginType.GOOGLE, "code-1")).isEqualTo(profile);
		assertThat(cache.get(LoginType.GOOGLE, "code-2")).isNull();
		assertThat(cache.get(LoginType.KAKAO, "code-1")).isNull();
	}

	@Test
	void expiresAfterTtl() {
		SocialAuthorizationCodeCache cache = new SocialAuthorizationCodeCache(Clock.systemUTC(), Duration.ZERO);
		SocialUserProfile profile = new SocialUserProfile("sub-1", "user@example.com", null, "사용자");

		cache.put(LoginType.GOOGLE, "code-1", profile);

		assertThat(cache.get(LoginType.GOOGLE, "code-1")).isNull();
	}
}
