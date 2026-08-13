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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.MemberAgreementUseCase;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.MemberTermAgreementPort;
import com.planwith.planwith_fo_member.application.port.out.TermsRepositoryPort;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;
import com.planwith.planwith_fo_member.domain.terms.Term;

@Service
@Transactional
public class MemberAgreementQueryService implements MemberAgreementUseCase {

	private final MemberRepositoryPort memberRepository;
	private final MemberTermAgreementPort agreementPort;
	private final TermsRepositoryPort termsRepository;

	public MemberAgreementQueryService(
			MemberRepositoryPort memberRepository,
			MemberTermAgreementPort agreementPort,
			TermsRepositoryPort termsRepository
	) {
		this.memberRepository = memberRepository;
		this.agreementPort = agreementPort;
		this.termsRepository = termsRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<AgreementView> list(UUID memberUuid) {
		requireActiveMember(memberUuid);
		Map<Long, MemberTermAgreementPort.StoredAgreement> byTermId = agreementPort.findByMemberUuid(memberUuid).stream()
				.collect(Collectors.toMap(MemberTermAgreementPort.StoredAgreement::termId, Function.identity()));

		return termsRepository.findActiveTerms(null).stream()
				.map(term -> {
					MemberTermAgreementPort.StoredAgreement stored = byTermId.get(term.getTermId());
					boolean agreed = stored != null && stored.agreed();
					Instant agreedAt = stored == null ? null : stored.agreedAt();
					return toView(term, agreed, agreedAt);
				})
				.toList();
	}

	@Override
	public List<AgreementView> upsertOptional(UUID memberUuid, List<AgreementInput> agreements) {
		requireActiveMember(memberUuid);
		if (agreements == null || agreements.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "약관 동의 목록이 비어 있습니다.");
		}

		Set<UUID> seen = new HashSet<>();
		List<MemberTermAgreementPort.AgreementCommand> commands = new ArrayList<>();
		Instant now = Instant.now();

		for (AgreementInput item : agreements) {
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
			if (term.isRequired()) {
				throw new BusinessException(ErrorCode.REQUIRED_TERM_NOT_MODIFIABLE);
			}
			commands.add(new MemberTermAgreementPort.AgreementCommand(
					term.getTermId(),
					item.agreed(),
					item.agreed() ? now : null
			));
		}

		agreementPort.upsertAgreements(memberUuid, commands);
		return list(memberUuid);
	}

	private void requireActiveMember(UUID memberUuid) {
		Member member = memberRepository.findByUuid(memberUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		if (member.getStatus() != MemberStatus.ACTIVE) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
		}
	}

	private AgreementView toView(Term term, boolean agreed, Instant agreedAt) {
		return new AgreementView(
				term.getTermUuid(),
				term.getTitle(),
				term.getTermType(),
				term.getVersion(),
				term.isRequired(),
				agreed,
				agreedAt
		);
	}
}
