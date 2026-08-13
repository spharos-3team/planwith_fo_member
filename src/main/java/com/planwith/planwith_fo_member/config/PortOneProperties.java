package com.planwith.planwith_fo_member.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.portone")
public record PortOneProperties(
		String storeId,
		String channelKey,
		String apiSecret,
		String apiBaseUrl,
		int verifiedTtlSeconds,
		boolean stubEnabled
) {
}
