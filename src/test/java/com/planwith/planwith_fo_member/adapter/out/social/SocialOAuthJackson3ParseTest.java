package com.planwith.planwith_fo_member.adapter.out.social;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class SocialOAuthJackson3ParseTest {

	@Test
	void jackson3ReadsAccessTokenFromProviderJson() {
		JsonMapper mapper = JsonMapper.builder().build();
		JsonNode token = mapper.readTree("""
				{"access_token":"ya29.example","token_type":"Bearer"}
				""");

		assertThat(token.get("access_token").asString()).isEqualTo("ya29.example");
	}
}
