package com.planwith.planwith_fo_member.domain.member;

import java.util.UUID;

public class MemberProfile {

	private final Long memberId;
	private final UUID memberUuid;
	private final String nickname;
	private final String profileImage;
	private final String profileIntro;
	private final String grade;

	public MemberProfile(
			Long memberId,
			UUID memberUuid,
			String nickname,
			String profileImage,
			String profileIntro,
			String grade
	) {
		this.memberId = memberId;
		this.memberUuid = memberUuid;
		this.nickname = nickname;
		this.profileImage = profileImage;
		this.profileIntro = profileIntro;
		this.grade = grade;
	}

	public Long getMemberId() {
		return memberId;
	}

	public UUID getMemberUuid() {
		return memberUuid;
	}

	public String getNickname() {
		return nickname;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public String getProfileIntro() {
		return profileIntro;
	}

	public String getGrade() {
		return grade;
	}
}
