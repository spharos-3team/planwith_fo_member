package com.planwith.planwith_fo_member.application.member;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.application.auth.AuthSessionService;
import com.planwith.planwith_fo_member.application.auth.PhoneVerificationService;
import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.LocalLoginUseCase.AuthTokenResult;
import com.planwith.planwith_fo_member.application.port.in.SocialSignupUseCase;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.MemberTermAgreementPort;
import com.planwith.planwith_fo_member.application.port.out.PhoneVerificationStorePort;
import com.planwith.planwith_fo_member.application.port.out.SocialOAuthClientPort;
import com.planwith.planwith_fo_member.domain.member.LoginType;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberProfile;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

@Service
@Transactional
public class SocialSignupService implements SocialSignupUseCase {

	private final SocialOAuthClientPort socialOAuthClient;
	private final MemberRepositoryPort memberRepository;
	private final MemberTermAgreementPort agreementPort;
	private final PhoneVerificationStorePort phoneVerificationStore;
	private final PhoneVerificationService phoneVerificationService;
	private final TermsAgreementService termsAgreementService;
	private final AuthSessionService authSessionService;

	public SocialSignupService(
			SocialOAuthClientPort socialOAuthClient,
			MemberRepositoryPort memberRepository,
			MemberTermAgreementPort agreementPort,
			PhoneVerificationStorePort phoneVerificationStore,
			PhoneVerificationService phoneVerificationService,
			TermsAgreementService termsAgreementService,
			AuthSessionService authSessionService
	) {
		this.socialOAuthClient = socialOAuthClient;
		this.memberRepository = memberRepository;
		this.agreementPort = agreementPort;
		this.phoneVerificationStore = phoneVerificationStore;
		this.phoneVerificationService = phoneVerificationService;
		this.termsAgreementService = termsAgreementService;
		this.authSessionService = authSessionService;
	}

	@Override
	public SocialSignupResult signup(LoginType provider, SocialSignupCommand command) {
		if (provider == null || provider == LoginType.LOCAL) {
			throw new BusinessException(ErrorCode.UNSUPPORTED_PROVIDER);
		}

		String nickname = command.nickname().trim();
		PhoneVerificationStorePort.VerifiedPhone verifiedPhone = phoneVerificationService.requireMatchingVerified(
				command.phoneNumber(),
				command.name()
		);
		String phoneNumber = verifiedPhone.phoneNumber();

		SocialOAuthClientPort.SocialUserProfile socialUser = socialOAuthClient.fetchUser(
				provider,
				command.authorizationCode(),
				command.redirectUri()
		);
		if (!StringUtils.hasText(socialUser.socialId())) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED, "소셜 계정 식별자를 확인할 수 없습니다.");
		}
		if (!StringUtils.hasText(socialUser.email())) {
			throw new BusinessException(ErrorCode.SOCIAL_EMAIL_REQUIRED);
		}

		String email = socialUser.email().trim().toLowerCase();
		if (memberRepository.existsByLoginTypeAndSocialId(provider, socialUser.socialId())) {
			throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS);
		}
		if (memberRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}
		if (memberRepository.existsByNickname(nickname)) {
			throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
		}

		List<MemberTermAgreementPort.AgreementCommand> agreementCommands = termsAgreementService.resolve(
				command.agreements().stream()
						.map(item -> new TermsAgreementService.AgreementInput(item.termUuid(), item.agreed()))
						.toList()
		);

		String profileImage = blankToNull(command.profileImage());
		if (profileImage == null) {
			profileImage = blankToNull(socialUser.profileImageUrl());
		}

		UUID memberUuid = UUID.randomUUID();
		Instant createdAt = Instant.now();
		Member member = new Member(
				null,
				memberUuid,
				provider,
				email,
				null,
				phoneNumber,
				verifiedPhone.name(),
				socialUser.socialId(),
				MemberStatus.ACTIVE,
				createdAt
		);
		MemberProfile profile = new MemberProfile(
				null,
				memberUuid,
				nickname,
				profileImage,
				blankToNull(command.profileIntro()),
				"SEED"
		);

		Member saved = memberRepository.saveMember(member, profile);
		agreementPort.saveAgreements(saved.getMemberUuid(), agreementCommands);
		phoneVerificationStore.clear(phoneNumber);

		AuthTokenResult tokens = authSessionService.issueSession(saved.getMemberUuid());
		return new SocialSignupResult(
				saved.getMemberUuid(),
				saved.getEmail(),
				nickname,
				saved.getCreatedAt(),
				tokens
		);
	}

	private String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
