package com.planwith.planwith_fo_member.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public record EmailProperties(
		boolean stubEnabled,
		String from
) {
}
