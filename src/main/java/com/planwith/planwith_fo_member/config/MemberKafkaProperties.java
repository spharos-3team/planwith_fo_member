package com.planwith.planwith_fo_member.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record MemberKafkaProperties(
		boolean enabled,
		String memberCreatedTopic
) {
}
