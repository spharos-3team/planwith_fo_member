package com.planwith.planwith_fo_member.application.port.in;

public interface SendEmailVerificationUseCase {

	SendEmailVerificationResult send(String email);

	record SendEmailVerificationResult(String email, int expiresInSeconds) {
	}
}
