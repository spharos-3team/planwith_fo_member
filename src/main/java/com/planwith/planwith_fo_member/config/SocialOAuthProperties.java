package com.planwith.planwith_fo_member.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.social")
public record SocialOAuthProperties(
		boolean stubEnabled,
		ProviderProperties google,
		ProviderProperties kakao,
		ProviderProperties naver
) {
	public record ProviderProperties(
			String clientId,
			String clientSecret,
			String tokenUri,
			String userInfoUri
	) {
	}
}
