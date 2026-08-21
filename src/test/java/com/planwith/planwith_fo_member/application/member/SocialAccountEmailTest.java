package com.planwith.planwith_fo_member.application.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_member.domain.member.LoginType;

class SocialAccountEmailTest {

	@Test
	void usesProviderEmailWhenPresentAndUnused() {
		assertThat(SocialAccountEmail.resolve(LoginType.NAVER, "123", "user@gmail.com", false))
				.isEqualTo("user@gmail.com");
	}

	@Test
	void fallsBackWhenProviderEmailMissing() {
		assertThat(SocialAccountEmail.resolve(LoginType.NAVER, "123", "  ", false))
				.isEqualTo("social.naver.123@users.planwith");
	}

	@Test
	void fallsBackWhenProviderEmailAlreadyTaken() {
		assertThat(SocialAccountEmail.resolve(LoginType.NAVER, "123", "user@gmail.com", true))
				.isEqualTo("social.naver.123@users.planwith");
	}
}
