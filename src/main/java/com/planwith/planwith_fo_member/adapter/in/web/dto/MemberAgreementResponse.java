package com.planwith.planwith_fo_member.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

public record MemberAgreementResponse(
		UUID termUuid,
		String title,
		String termType,
		String version,
		boolean required,
		boolean agreed,
		Instant agreedAt
) {
}
