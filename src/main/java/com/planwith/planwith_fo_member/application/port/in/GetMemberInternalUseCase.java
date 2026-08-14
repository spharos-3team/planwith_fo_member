package com.planwith.planwith_fo_member.application.port.in;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_member.domain.member.LoginType;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

public interface GetMemberInternalUseCase {

	InternalMemberResult getInternal(UUID memberUuid);

	record InternalMemberResult(
			UUID memberUuid,
			String email,
			String phoneNumber,
			String name,
			LoginType loginType,
			MemberStatus status,
			Instant createdAt
	) {
	}
}
