package com.planwith.planwith_fo_member.application.auth;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.LocalLoginUseCase.AuthTokenResult;
import com.planwith.planwith_fo_member.application.port.in.ReissueTokenUseCase;
import com.planwith.planwith_fo_member.application.port.out.AccessTokenIssuerPort;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.RefreshTokenStorePort;
import com.planwith.planwith_fo_member.config.JwtProperties;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

@Service
@Transactional
public class ReissueTokenService implements ReissueTokenUseCase {

	private final RefreshTokenStorePort refreshTokenStore;
	private final AccessTokenIssuerPort accessTokenIssuer;
	private final MemberRepositoryPort memberRepository;
	private final JwtProperties jwtProperties;

	public ReissueTokenService(
			RefreshTokenStorePort refreshTokenStore,
			AccessTokenIssuerPort accessTokenIssuer,
			MemberRepositoryPort memberRepository,
			JwtProperties jwtProperties
	) {
		this.refreshTokenStore = refreshTokenStore;
		this.accessTokenIssuer = accessTokenIssuer;
		this.memberRepository = memberRepository;
		this.jwtProperties = jwtProperties;
	}

	@Override
	public AuthTokenResult reissue(String refreshToken) {
		if (!StringUtils.hasText(refreshToken)) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}

		RefreshTokenStorePort.IssuedRefreshToken rotated = refreshTokenStore.rotate(
				refreshToken,
				Instant.now().plus(jwtProperties.refreshTokenTtl())
		);

		Member member = memberRepository.findByUuid(rotated.memberUuid())
				.orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

		if (member.getStatus() != MemberStatus.ACTIVE) {
			refreshTokenStore.revokeFamily(rotated.familyId());
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}

		AccessTokenIssuerPort.IssuedAccessToken access = accessTokenIssuer.issue(member.getMemberUuid(), rotated.sessionId());
		return new AuthTokenResult(
				access.accessToken(),
				access.expiresInSeconds(),
				rotated.rawToken(),
				member.getMemberUuid(),
				access.roles(),
				access.scopes()
		);
	}
}
