package com.planwith.planwith_fo_member.adapter.out.persistence.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "member_profile")
public class MemberProfileJpaEntity {

	@Id
	@Column(name = "member_id")
	private Long memberId;

	@Column(name = "member_uuid", nullable = false, unique = true, length = 36)
	private String memberUuid;

	@Column(name = "nickname", nullable = false, unique = true, length = 20)
	private String nickname;

	@Column(name = "profile_image", length = 1000)
	private String profileImage;

	@Column(name = "profile_intro", length = 100)
	private String profileIntro;

	@Column(name = "grade", nullable = false, length = 30)
	private String grade;

	@Column(name = "profile_badge", nullable = false)
	private boolean profileBadge;

	@Column(name = "profile_special_border", nullable = false)
	private boolean profileSpecialBorder;

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public String getMemberUuid() {
		return memberUuid;
	}

	public void setMemberUuid(String memberUuid) {
		this.memberUuid = memberUuid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public String getProfileIntro() {
		return profileIntro;
	}

	public void setProfileIntro(String profileIntro) {
		this.profileIntro = profileIntro;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public boolean isProfileBadge() {
		return profileBadge;
	}

	public void setProfileBadge(boolean profileBadge) {
		this.profileBadge = profileBadge;
	}

	public boolean isProfileSpecialBorder() {
		return profileSpecialBorder;
	}

	public void setProfileSpecialBorder(boolean profileSpecialBorder) {
		this.profileSpecialBorder = profileSpecialBorder;
	}
}
