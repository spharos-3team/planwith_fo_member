package com.planwith.planwith_fo_member.domain.member;

import java.time.Instant;
import java.util.UUID;

public class Member {

	private final Long memberId;
	private final UUID memberUuid;
	private final LoginType loginType;
	private final String email;
	private final String passwordHash;
	private final String phoneNumber;
	private final String socialId;
	private final MemberStatus status;
	private final Instant createdAt;
	private final Instant lastLoginAt;

	public Member(
			Long memberId,
			UUID memberUuid,
			LoginType loginType,
			String email,
			String passwordHash,
			String phoneNumber,
			String socialId,
			MemberStatus status,
			Instant createdAt
	) {
		this(memberId, memberUuid, loginType, email, passwordHash, phoneNumber, socialId, status, createdAt, null);
	}

	public Member(
			Long memberId,
			UUID memberUuid,
			LoginType loginType,
			String email,
			String passwordHash,
			String phoneNumber,
			String socialId,
			MemberStatus status,
			Instant createdAt,
			Instant lastLoginAt
	) {
		this.memberId = memberId;
		this.memberUuid = memberUuid;
		this.loginType = loginType;
		this.email = email;
		this.passwordHash = passwordHash;
		this.phoneNumber = phoneNumber;
		this.socialId = socialId;
		this.status = status;
		this.createdAt = createdAt;
		this.lastLoginAt = lastLoginAt;
	}

	public Long getMemberId() {
		return memberId;
	}

	public UUID getMemberUuid() {
		return memberUuid;
	}

	public LoginType getLoginType() {
		return loginType;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getSocialId() {
		return socialId;
	}

	public MemberStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getLastLoginAt() {
		return lastLoginAt;
	}
}
