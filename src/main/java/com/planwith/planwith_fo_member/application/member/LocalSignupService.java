package com.planwith.planwith_fo_member.application.member;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.LocalSignupUseCase;
import com.planwith.planwith_fo_member.application.auth.PhoneVerificationService;
import com.planwith.planwith_fo_member.application.port.out.EmailVerificationStorePort;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.MemberTermAgreementPort;
import com.planwith.planwith_fo_member.application.port.out.PhoneVerificationStorePort;
import com.planwith.planwith_fo_member.application.port.out.TermsRepositoryPort;
import com.planwith.planwith_fo_member.domain.member.LoginType;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberProfile;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;
import com.planwith.planwith_fo_member.domain.terms.Term;

@Service
@Transactional
public class LocalSignupService implements LocalSignupUseCase {

	private final MemberRepositoryPort memberRepository;
	private final TermsRepositoryPort termsRepository;
	private final MemberTermAgreementPort agreementPort;
	private final EmailVerificationStorePort verificationStore;
	private final PhoneVerificationStorePort phoneVerificationStore;
	private final PasswordEncoder passwordEncoder;

	public LocalSignupService(
			MemberRepositoryPort memberRepository,
			TermsRepositoryPort termsRepository,
			MemberTermAgreementPort agreementPort,
			EmailVerificationStorePort verificationStore,
			PhoneVerificationStorePort phoneVerificationStore,
			PasswordEncoder passwordEncoder
	) {
		this.memberRepository = memberRepository;
		this.termsRepository = termsRepository;
		this.agreementPort = agreementPort;
		this.verificationStore = verificationStore;
		this.phoneVerificationStore = phoneVerificationStore;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public LocalSignupResult signup(LocalSignupCommand command) {
		String email = command.email().trim().toLowerCase();
		String nickname = command.nickname().trim();
		String phoneNumber = PhoneVerificationService.normalizePhone(command.phoneNumber());

		if (!verificationStore.isVerified(email)) {
			throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
		}
		if (phoneNumber == null || phoneNumber.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "휴대폰 번호는 필수입니다.");
		}
		if (!phoneVerificationStore.isVerified(phoneNumber)) {
			throw new BusinessException(ErrorCode.PHONE_NOT_VERIFIED);
		}
		if (memberRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}
		if (memberRepository.existsByNickname(nickname)) {
			throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
		}

		List<MemberTermAgreementPort.AgreementCommand> agreementCommands = resolveAgreements(command.agreements());

		UUID memberUuid = UUID.randomUUID();
		Instant createdAt = Instant.now();
		Member member = new Member(
				null,
				memberUuid,
				LoginType.LOCAL,
				email,
				passwordEncoder.encode(command.password()),
				phoneNumber,
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

		Member saved = memberRepository.saveLocalMember(member, profile);
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

	private List<MemberTermAgreementPort.AgreementCommand> resolveAgreements(List<AgreementItem> agreements) {
		if (agreements == null || agreements.isEmpty()) {
			throw new BusinessException(ErrorCode.REQUIRED_TERM_NOT_AGREED);
		}

		Set<UUID> seen = new HashSet<>();
		List<MemberTermAgreementPort.AgreementCommand> commands = new ArrayList<>();
		Instant agreedAt = Instant.now();

		for (AgreementItem item : agreements) {
			if (item.termUuid() == null) {
				throw new BusinessException(ErrorCode.INVALID_REQUEST, "termUuid는 필수입니다.");
			}
			if (!seen.add(item.termUuid())) {
				throw new BusinessException(ErrorCode.INVALID_REQUEST, "중복된 약관 동의 요청입니다.");
			}

			Term term = termsRepository.findByUuid(item.termUuid())
					.orElseThrow(() -> new BusinessException(ErrorCode.TERM_NOT_FOUND));
			if (!term.isActive()) {
				throw new BusinessException(ErrorCode.TERM_INACTIVE);
			}
			commands.add(new MemberTermAgreementPort.AgreementCommand(term.getTermId(), item.agreed(), item.agreed() ? agreedAt : null));
		}

		Map<UUID, AgreementItem> agreedByUuid = agreements.stream()
				.collect(Collectors.toMap(AgreementItem::termUuid, Function.identity()));

		for (Term required : termsRepository.findActiveRequiredTerms()) {
			AgreementItem item = agreedByUuid.get(required.getTermUuid());
			if (item == null || !item.agreed()) {
				throw new BusinessException(ErrorCode.REQUIRED_TERM_NOT_AGREED);
			}
		}

		return commands;
	}

	private String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
