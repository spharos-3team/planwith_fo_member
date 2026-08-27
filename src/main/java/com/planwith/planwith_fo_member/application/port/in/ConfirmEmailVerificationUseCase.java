package com.planwith.planwith_fo_member.application.port.in;

public interface ConfirmEmailVerificationUseCase {

	ConfirmEmailVerificationResult confirm(String email, String code);

	record ConfirmEmailVerificationResult(String email, boolean verified, int verifiedExpiresInSeconds) {
	}
}
