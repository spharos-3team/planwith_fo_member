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

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.out.MemberTermAgreementPort;
import com.planwith.planwith_fo_member.application.port.out.TermsRepositoryPort;
import com.planwith.planwith_fo_member.domain.terms.Term;

@Service
public class TermsAgreementService {

	private final TermsRepositoryPort termsRepository;

	public TermsAgreementService(TermsRepositoryPort termsRepository) {
		this.termsRepository = termsRepository;
	}

	public List<MemberTermAgreementPort.AgreementCommand> resolve(List<AgreementInput> agreements) {
		if (agreements == null || agreements.isEmpty()) {
			throw new BusinessException(ErrorCode.REQUIRED_TERM_NOT_AGREED);
		}

		Set<UUID> seen = new HashSet<>();
		List<MemberTermAgreementPort.AgreementCommand> commands = new ArrayList<>();
		Instant agreedAt = Instant.now();

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
			commands.add(new MemberTermAgreementPort.AgreementCommand(
					term.getTermId(),
					item.agreed(),
					item.agreed() ? agreedAt : null
			));
		}

		Map<UUID, AgreementInput> agreedByUuid = agreements.stream()
				.collect(Collectors.toMap(AgreementInput::termUuid, Function.identity()));

		for (Term required : termsRepository.findActiveRequiredTerms()) {
			AgreementInput item = agreedByUuid.get(required.getTermUuid());
			if (item == null || !item.agreed()) {
				throw new BusinessException(ErrorCode.REQUIRED_TERM_NOT_AGREED);
			}
		}

		return commands;
	}

	public record AgreementInput(UUID termUuid, boolean agreed) {
	}
}
