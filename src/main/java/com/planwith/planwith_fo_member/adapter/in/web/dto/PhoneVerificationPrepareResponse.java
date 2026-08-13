package com.planwith.planwith_fo_member.adapter.in.web.dto;

public record PhoneVerificationPrepareResponse(
		String storeId,
		String channelKey,
		String identityVerificationId
) {
}
