package com.planwith.planwith_fo_member.application.port.in;

public interface ConfirmPhoneVerificationUseCase {

	ConfirmPhoneVerificationResult confirm(String identityVerificationId);

	record ConfirmPhoneVerificationResult(
			boolean verified,
			String phoneNumber,
			String maskedPhoneNumber,
			String name
	) {
	}
}
