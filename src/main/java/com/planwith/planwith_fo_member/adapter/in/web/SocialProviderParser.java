package com.planwith.planwith_fo_member.adapter.in.web;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.domain.member.LoginType;

final class SocialProviderParser {

	private SocialProviderParser() {
	}

	static LoginType parse(String provider) {
		if (provider == null || provider.isBlank()) {
			throw new BusinessException(ErrorCode.UNSUPPORTED_PROVIDER);
		}
		try {
			LoginType loginType = LoginType.valueOf(provider.trim().toUpperCase());
			if (loginType == LoginType.LOCAL) {
				throw new BusinessException(ErrorCode.UNSUPPORTED_PROVIDER);
			}
			return loginType;
		}
		catch (IllegalArgumentException exception) {
			throw new BusinessException(ErrorCode.UNSUPPORTED_PROVIDER);
		}
	}
}
