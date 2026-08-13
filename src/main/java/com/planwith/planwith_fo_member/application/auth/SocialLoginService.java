package com.planwith.planwith_fo_member.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.LocalLoginUseCase.AuthTokenResult;
import com.planwith.planwith_fo_member.application.port.in.SocialLoginUseCase;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.SocialOAuthClientPort;
import com.planwith.planwith_fo_member.domain.member.LoginType;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

@Service
@Transactional
public class SocialLoginService implements SocialLoginUseCase {

	private final SocialOAuthClientPort socialOAuthClient;
	private final MemberRepositoryPort memberRepository;
	private final AuthSessionService authSessionService;

	public SocialLoginService(
			SocialOAuthClientPort socialOAuthClient,
			MemberRepositoryPort memberRepository,
			AuthSessionService authSessionService
	) {
		this.socialOAuthClient = socialOAuthClient;
		this.memberRepository = memberRepository;
		this.authSessionService = authSessionService;
	}

	@Override
	public SocialLoginResult login(LoginType provider, SocialLoginCommand command) {
		if (provider == null || provider == LoginType.LOCAL) {
			throw new BusinessException(ErrorCode.UNSUPPORTED_PROVIDER);
		}

		SocialOAuthClientPort.SocialUserProfile socialUser = socialOAuthClient.fetchUser(
				provider,
				command.authorizationCode(),
				command.redirectUri()
		);
		if (!StringUtils.hasText(socialUser.socialId())) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED, "소셜 계정 식별자를 확인할 수 없습니다.");
		}

		return memberRepository.findByLoginTypeAndSocialId(provider, socialUser.socialId())
				.map(this::loginExisting)
				.orElseGet(() -> new SocialLoginResult(true, null));
	}

	private SocialLoginResult loginExisting(Member member) {
		if (member.getStatus() != MemberStatus.ACTIVE) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		AuthTokenResult tokens = authSessionService.issueSession(member.getMemberUuid());
		return new SocialLoginResult(false, tokens);
	}
}
