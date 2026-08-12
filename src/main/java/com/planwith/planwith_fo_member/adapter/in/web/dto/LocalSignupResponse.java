package com.planwith.planwith_fo_member.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

public record LocalSignupResponse(
		UUID memberUuid,
		String email,
		String nickname,
		Instant createdAt
) {
}
