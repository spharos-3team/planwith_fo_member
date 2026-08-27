package com.planwith.planwith_fo_member.domain.member;

import java.util.Locale;
import java.util.UUID;

public class MemberProfile {

	public static final String INITIAL_GRADE = "ROOKIE";

	private final Long memberId;
	private final UUID memberUuid;
	private final String nickname;
	private final String profileImage;
	private final String profileIntro;
	private final String grade;
	private final boolean profileBadge;
	private final boolean profileSpecialBorder;

	public MemberProfile(
			Long memberId,
			UUID memberUuid,
			String nickname,
			String profileImage,
			String profileIntro,
			String grade
	) {
		this(memberId, memberUuid, nickname, profileImage, profileIntro, grade, false, false);
	}

	public MemberProfile(
			Long memberId,
			UUID memberUuid,
			String nickname,
			String profileImage,
			String profileIntro,
			String grade,
			boolean profileBadge,
			boolean profileSpecialBorder
	) {
		this.memberId = memberId;
		this.memberUuid = memberUuid;
		this.nickname = nickname;
		this.profileImage = sanitizeProfileImage(profileImage);
		this.profileIntro = profileIntro;
		this.grade = grade;
		this.profileBadge = profileBadge;
		this.profileSpecialBorder = profileSpecialBorder;
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

	public boolean isProfileBadge() {
		return profileBadge;
	}

	public boolean isProfileSpecialBorder() {
		return profileSpecialBorder;
	}

	public static String sanitizeProfileImage(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String trimmed = value.trim();
		String lower = trimmed.toLowerCase(Locale.ROOT);
		if (lower.startsWith("http://") || lower.startsWith("https://")) {
			return null;
		}
		return trimmed;
	}
}
