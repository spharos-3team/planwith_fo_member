package com.planwith.planwith_fo_member.adapter.in.web.dto;

import java.util.UUID;

public record TermResponse(
		UUID termUuid,
		String title,
		String termType,
		String version,
		boolean isRequired,
		boolean isActive
) {
}
