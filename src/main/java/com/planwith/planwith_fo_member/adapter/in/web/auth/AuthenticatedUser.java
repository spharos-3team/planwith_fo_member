package com.planwith.planwith_fo_member.adapter.in.web.auth;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(
		UUID userId,
		List<String> roles,
		List<String> scopes,
		String sessionId,
		String requestId
) {
}
