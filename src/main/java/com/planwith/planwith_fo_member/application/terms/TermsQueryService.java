package com.planwith.planwith_fo_member.application.terms;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.port.in.ListTermsUseCase;
import com.planwith.planwith_fo_member.application.port.out.TermsRepositoryPort;
import com.planwith.planwith_fo_member.domain.terms.Term;

@Service
@Transactional(readOnly = true)
public class TermsQueryService implements ListTermsUseCase {

	private final TermsRepositoryPort termsRepository;

	public TermsQueryService(TermsRepositoryPort termsRepository) {
		this.termsRepository = termsRepository;
	}

	@Override
	public List<Term> list(String termType) {
		String normalizedType = (termType == null || termType.isBlank()) ? null : termType.trim();
		return termsRepository.findActiveTerms(normalizedType);
	}
}
