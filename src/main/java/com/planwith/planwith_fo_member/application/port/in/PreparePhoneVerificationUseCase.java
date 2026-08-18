package com.planwith.planwith_fo_member.application.port.in;

public interface PreparePhoneVerificationUseCase {

	PreparePhoneVerificationResult prepare(String stubPhoneNumber, String stubName);

	record PreparePhoneVerificationResult(
			String storeId,
			String channelKey,
			String identityVerificationId
	) {
	}
}
