package com.planwith.planwith_fo_member.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.refresh-cookie")
public record RefreshCookieProperties(
		String name,
		boolean secure,
		String sameSite,
		String domain,
		String path
) {
}
