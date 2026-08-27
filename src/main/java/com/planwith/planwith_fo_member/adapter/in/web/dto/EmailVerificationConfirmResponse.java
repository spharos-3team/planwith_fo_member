package com.planwith.planwith_fo_member.adapter.in.web.dto;

public record EmailVerificationConfirmResponse(
		String email,
		boolean verified,
		int verifiedExpiresInSeconds
) {
}
