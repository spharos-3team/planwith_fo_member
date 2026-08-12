package com.planwith.planwith_fo_member.adapter.in.web.dto;

public record EmailVerificationSendResponse(
		String email,
		int expiresInSeconds,
		String message
) {
}
