package com.planwith.planwith_fo_member.application.port.in;

import com.planwith.planwith_fo_member.domain.member.LoginType;

public interface FindEmailUseCase {

	record FindEmailResult(String email, String maskedEmail, LoginType loginType) {
	}

	FindEmailResult findByVerifiedPhone(String phoneNumber);
}
