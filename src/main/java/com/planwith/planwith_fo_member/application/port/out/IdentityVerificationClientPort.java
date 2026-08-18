package com.planwith.planwith_fo_member.application.port.out;

public interface IdentityVerificationClientPort {

	VerifiedIdentity fetchVerified(String identityVerificationId);

	/**
	 * 스텁에서만 phone/name 오버라이드를 반영한다. 실연동 구현은 id만 사용한다.
	 */
	default VerifiedIdentity fetchVerified(String identityVerificationId, String stubPhoneNumber, String stubName) {
		return fetchVerified(identityVerificationId);
	}

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
