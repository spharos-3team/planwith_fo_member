package com.planwith.planwith_fo_member.adapter.out.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.config.JwtProperties;

@Component
public class JwtSecretKeyProvider {

	private static final int MIN_SECRET_BYTES = 32;
	private static final String HMAC_ALGORITHM = "HmacSHA256";

	private final SecretKey secretKey;

	public JwtSecretKeyProvider(JwtProperties properties) {
		this.secretKey = createSecretKey(properties.secret());
	}

	public SecretKey secretKey() {
		return secretKey;
	}

	static SecretKey createSecretKey(String secret) {
		if (!StringUtils.hasText(secret)) {
			throw new IllegalStateException("JWT_SECRET must be configured.");
		}
		byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
		if (secretBytes.length < MIN_SECRET_BYTES) {
			throw new IllegalStateException("JWT_SECRET must be at least 32 bytes.");
		}
		return new SecretKeySpec(secretBytes, HMAC_ALGORITHM);
	}
}
