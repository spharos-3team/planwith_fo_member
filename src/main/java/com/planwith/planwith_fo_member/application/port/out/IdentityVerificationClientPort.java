package com.planwith.planwith_fo_member.application.port.out;

public interface IdentityVerificationClientPort {

	VerifiedIdentity fetchVerified(String identityVerificationId);

	record VerifiedIdentity(
			String identityVerificationId,
			String status,
			String phoneNumber,
			String name
	) {
		public boolean isVerified() {
			return "VERIFIED".equalsIgnoreCase(status);
		}
	}
}
