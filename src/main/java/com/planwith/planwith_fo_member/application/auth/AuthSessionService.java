package com.planwith.planwith_fo_member.application.auth;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.port.in.LocalLoginUseCase.AuthTokenResult;
import com.planwith.planwith_fo_member.application.port.out.AccessTokenIssuerPort;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.RefreshTokenStorePort;
import com.planwith.planwith_fo_member.config.JwtProperties;

@Service
@Transactional
public class AuthSessionService {

	private final AccessTokenIssuerPort accessTokenIssuer;
	private final RefreshTokenStorePort refreshTokenStore;
	private final MemberRepositoryPort memberRepository;
	private final JwtProperties jwtProperties;

	public AuthSessionService(
			AccessTokenIssuerPort accessTokenIssuer,
			RefreshTokenStorePort refreshTokenStore,
			MemberRepositoryPort memberRepository,
			JwtProperties jwtProperties
	) {
		this.accessTokenIssuer = accessTokenIssuer;
		this.refreshTokenStore = refreshTokenStore;
		this.memberRepository = memberRepository;
		this.jwtProperties = jwtProperties;
	}

	public AuthTokenResult issueSession(UUID memberUuid) {
		Instant refreshExpiresAt = Instant.now().plus(jwtProperties.refreshTokenTtl());
		RefreshTokenStorePort.IssuedRefreshToken refresh = refreshTokenStore.issue(memberUuid, refreshExpiresAt);
		AccessTokenIssuerPort.IssuedAccessToken access = accessTokenIssuer.issue(memberUuid, refresh.sessionId());
		memberRepository.updateLastLoginAt(memberUuid, Instant.now());
		return new AuthTokenResult(
				access.accessToken(),
				access.expiresInSeconds(),
				refresh.rawToken(),
				memberUuid,
				access.roles(),
				access.scopes()
		);
	}
}
