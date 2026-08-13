package com.planwith.planwith_fo_member.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
		String issuer,
		String audience,
		String keyId,
		String privateKeyPath,
		String publicKeyPath,
		Duration accessTokenTtl,
		Duration refreshTokenTtl
) {
}
