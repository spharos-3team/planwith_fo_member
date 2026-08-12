package com.planwith.planwith_fo_member.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email-verification")
public record EmailVerificationProperties(
		int codeTtlSeconds,
		int verifiedTtlSeconds,
		int codeLength
) {
}
