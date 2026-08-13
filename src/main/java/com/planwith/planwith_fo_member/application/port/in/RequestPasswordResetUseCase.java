package com.planwith.planwith_fo_member.application.port.in;

public interface RequestPasswordResetUseCase {

	record RequestPasswordResetResult(String email, long expiresInSeconds, String message) {
	}

	RequestPasswordResetResult request(String email);
}
