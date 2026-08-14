package com.planwith.planwith_fo_member.adapter.in.web.dto;

public record PhoneVerificationConfirmResponse(
		boolean verified,
		String phoneNumber,
		String maskedPhoneNumber,
		String name
) {
}
