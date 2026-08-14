package com.planwith.planwith_fo_member.application.member;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.auth.PhoneVerificationService;
import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.LocalSignupUseCase;
import com.planwith.planwith_fo_member.application.port.out.EmailVerificationStorePort;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.MemberTermAgreementPort;
import com.planwith.planwith_fo_member.application.port.out.PhoneVerificationStorePort;
import com.planwith.planwith_fo_member.domain.member.LoginType;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberProfile;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

@Service
@Transactional
public class LocalSignupService implements LocalSignupUseCase {

	private final MemberRepositoryPort memberRepository;
	private final MemberTermAgreementPort agreementPort;
	private final EmailVerificationStorePort verificationStore;
	private final PhoneVerificationStorePort phoneVerificationStore;
	private final PhoneVerificationService phoneVerificationService;
	private final TermsAgreementService termsAgreementService;
	private final PasswordEncoder passwordEncoder;

	public LocalSignupService(
			MemberRepositoryPort memberRepository,
			MemberTermAgreementPort agreementPort,
			EmailVerificationStorePort verificationStore,
			PhoneVerificationStorePort phoneVerificationStore,
			PhoneVerificationService phoneVerificationService,
			TermsAgreementService termsAgreementService,
			PasswordEncoder passwordEncoder
	) {
		this.memberRepository = memberRepository;
		this.agreementPort = agreementPort;
		this.verificationStore = verificationStore;
		this.phoneVerificationStore = phoneVerificationStore;
		this.phoneVerificationService = phoneVerificationService;
		this.termsAgreementService = termsAgreementService;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public LocalSignupResult signup(LocalSignupCommand command) {
		String email = command.email().trim().toLowerCase();
		String nickname = command.nickname().trim();

		if (!verificationStore.isVerified(email)) {
			throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
		}

		PhoneVerificationStorePort.VerifiedPhone verifiedPhone = phoneVerificationService.requireMatchingVerified(
				command.phoneNumber(),
				command.name()
		);
		String phoneNumber = verifiedPhone.phoneNumber();

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

		UUID memberUuid = UUID.randomUUID();
		Instant createdAt = Instant.now();
		Member member = new Member(
				null,
				memberUuid,
				LoginType.LOCAL,
				email,
				passwordEncoder.encode(command.password()),
				phoneNumber,
				verifiedPhone.name(),
				null,
				MemberStatus.ACTIVE,
				createdAt
		);
		MemberProfile profile = new MemberProfile(
				null,
				memberUuid,
				nickname,
				blankToNull(command.profileImage()),
				blankToNull(command.profileIntro()),
				"SEED"
		);

		Member saved = memberRepository.saveMember(member, profile);
		agreementPort.saveAgreements(saved.getMemberUuid(), agreementCommands);
		verificationStore.clear(email);
		phoneVerificationStore.clear(phoneNumber);

		return new LocalSignupResult(
				saved.getMemberUuid(),
				saved.getEmail(),
				nickname,
				saved.getCreatedAt()
		);
	}

	private String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
