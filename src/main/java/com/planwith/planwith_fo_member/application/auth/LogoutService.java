package com.planwith.planwith_fo_member.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.LogoutUseCase;
import com.planwith.planwith_fo_member.application.port.out.RefreshTokenStorePort;

@Service
@Transactional
public class LogoutService implements LogoutUseCase {

	private final RefreshTokenStorePort refreshTokenStore;

	public LogoutService(RefreshTokenStorePort refreshTokenStore) {
		this.refreshTokenStore = refreshTokenStore;
	}

	@Override
	public void logout(String refreshToken) {
		if (!StringUtils.hasText(refreshToken)) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		if (refreshTokenStore.findActive(refreshToken).isEmpty()) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		refreshTokenStore.revokeByRawToken(refreshToken);
	}
}
