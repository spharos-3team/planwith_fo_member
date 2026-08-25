package com.planwith.planwith_fo_member.application.auth;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.LocalLoginUseCase.AuthTokenResult;
import com.planwith.planwith_fo_member.application.port.out.AccessTokenIssuerPort;
import com.planwith.planwith_fo_member.application.port.out.ClientRequestInfoPort;
import com.planwith.planwith_fo_member.application.port.out.LoginHistoryRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.RefreshTokenStorePort;
import com.planwith.planwith_fo_member.config.JwtProperties;
import com.planwith.planwith_fo_member.domain.auth.LoginHistory;
import com.planwith.planwith_fo_member.domain.member.Member;

@Service
@Transactional
public class AuthSessionService {

	private final AccessTokenIssuerPort accessTokenIssuer;
	private final RefreshTokenStorePort refreshTokenStore;
	private final MemberRepositoryPort memberRepository;
	private final LoginHistoryRepositoryPort loginHistoryRepository;
	private final ClientRequestInfoPort clientRequestInfo;
	private final JwtProperties jwtProperties;

	public AuthSessionService(
			AccessTokenIssuerPort accessTokenIssuer,
			RefreshTokenStorePort refreshTokenStore,
			MemberRepositoryPort memberRepository,
			LoginHistoryRepositoryPort loginHistoryRepository,
			ClientRequestInfoPort clientRequestInfo,
			JwtProperties jwtProperties
	) {
		this.accessTokenIssuer = accessTokenIssuer;
		this.refreshTokenStore = refreshTokenStore;
		this.memberRepository = memberRepository;
		this.loginHistoryRepository = loginHistoryRepository;
		this.clientRequestInfo = clientRequestInfo;
		this.jwtProperties = jwtProperties;
	}

	public AuthTokenResult issueSession(UUID memberUuid) {
		Instant refreshExpiresAt = Instant.now().plus(jwtProperties.refreshTokenTtl());
		RefreshTokenStorePort.IssuedRefreshToken refresh = refreshTokenStore.issue(memberUuid, refreshExpiresAt);
		AccessTokenIssuerPort.IssuedAccessToken access = accessTokenIssuer.issue(memberUuid, refresh.sessionId());
		memberRepository.updateLastLoginAt(memberUuid, Instant.now());
		recordLoginHistory(memberUuid);
		return new AuthTokenResult(
				access.accessToken(),
				access.expiresInSeconds(),
				refresh.rawToken(),
				memberUuid,
				access.roles(),
				access.scopes()
		);
	}

	private void recordLoginHistory(UUID memberUuid) {
		Member member = memberRepository.findByUuid(memberUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		ClientRequestInfoPort.ClientRequestInfo requestInfo = clientRequestInfo.current();
		loginHistoryRepository.save(LoginHistory.user(
				member.getMemberId(),
				requestInfo.ipAddress(),
				requestInfo.userAgent()
		));
	}
}
