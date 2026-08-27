package com.planwith.planwith_fo_member.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class MemberProfileTest {

	@Test
	void remoteProfileUrlIsIgnored() {
		MemberProfile profile = new MemberProfile(
				1L,
				UUID.randomUUID(),
				"닉네임",
				"https://lh3.googleusercontent.com/photo",
				"소개",
				MemberProfile.INITIAL_GRADE
		);

		assertThat(profile.getProfileImage()).isNull();
	}

	@Test
	void storedProfilePathIsKept() {
		String stored = "/api/v1/members/11111111-1111-1111-1111-111111111111/profile-image";
		MemberProfile profile = new MemberProfile(
				1L,
				UUID.randomUUID(),
				"닉네임",
				stored,
				null,
				MemberProfile.INITIAL_GRADE
		);

		assertThat(profile.getProfileImage()).isEqualTo(stored);
	}
}
