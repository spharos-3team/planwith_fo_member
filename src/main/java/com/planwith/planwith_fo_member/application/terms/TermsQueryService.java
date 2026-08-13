package com.planwith.planwith_fo_member.application.terms;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.GetTermDetailUseCase;
import com.planwith.planwith_fo_member.application.port.in.ListTermsUseCase;
import com.planwith.planwith_fo_member.application.port.out.TermsRepositoryPort;
import com.planwith.planwith_fo_member.domain.terms.Term;

@Service
@Transactional(readOnly = true)
public class TermsQueryService implements ListTermsUseCase, GetTermDetailUseCase {

	private final TermsRepositoryPort termsRepository;

	public TermsQueryService(TermsRepositoryPort termsRepository) {
		this.termsRepository = termsRepository;
	}

	@Override
	public List<Term> list(String termType) {
		String normalizedType = (termType == null || termType.isBlank()) ? null : termType.trim();
		return termsRepository.findActiveTerms(normalizedType);
	}

	@Override
	public Term get(UUID termUuid) {
		Term term = termsRepository.findByUuid(termUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.TERM_NOT_FOUND));
		if (!term.isActive()) {
			throw new BusinessException(ErrorCode.TERM_INACTIVE);
		}
		return term;
	}
}
