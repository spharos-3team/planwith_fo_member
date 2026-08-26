package com.planwith.planwith_fo_member.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class JwtSecretKeyProviderTest {

	@Test
	void createsHmacSha256KeyForValidSecret() {
		String secret = "member-gateway-jwt-secret-at-least-32-bytes";

		var key = JwtSecretKeyProvider.createSecretKey(secret);

		assertThat(key.getAlgorithm()).isEqualTo("HmacSHA256");
		assertThat(key.getEncoded()).isEqualTo(secret.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void rejectsSecretShorterThan32Bytes() {
		assertThatThrownBy(() -> JwtSecretKeyProvider.createSecretKey("short-secret"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("at least 32 bytes");
	}

	@Test
	void rejectsMissingSecret() {
		assertThatThrownBy(() -> JwtSecretKeyProvider.createSecretKey(" "))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("must be configured");
	}
}
