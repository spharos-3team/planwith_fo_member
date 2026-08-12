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
	private final MemberStatus status;
	private final Instant createdAt;

	public Member(
			Long memberId,
			UUID memberUuid,
			LoginType loginType,
			String email,
			String passwordHash,
			String phoneNumber,
			MemberStatus status,
			Instant createdAt
	) {
		this.memberId = memberId;
		this.memberUuid = memberUuid;
		this.loginType = loginType;
		this.email = email;
		this.passwordHash = passwordHash;
		this.phoneNumber = phoneNumber;
		this.status = status;
		this.createdAt = createdAt;
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

	public MemberStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
