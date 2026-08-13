package com.planwith.planwith_fo_member.adapter.in.web.dto;

import java.util.UUID;

public record TermDetailResponse(
		UUID termUuid,
		String title,
		String termType,
		String version,
		String content,
		boolean isRequired,
		boolean isActive
) {
}
