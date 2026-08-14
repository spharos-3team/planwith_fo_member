package com.planwith.planwith_fo_member.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_member.domain.member.LoginType;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

public record InternalMemberResponse(
		UUID memberUuid,
		String email,
		String phoneNumber,
		String name,
		LoginType loginType,
		MemberStatus status,
		Instant createdAt
) {
}
