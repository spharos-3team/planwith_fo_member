package com.planwith.planwith_fo_member.adapter.out.persistence.terms;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.port.out.TermsRepositoryPort;
import com.planwith.planwith_fo_member.domain.terms.Term;

@Component
@Transactional(readOnly = true)
public class TermsPersistenceAdapter implements TermsRepositoryPort {

	private final TermsJpaRepository termsJpaRepository;

	public TermsPersistenceAdapter(TermsJpaRepository termsJpaRepository) {
		this.termsJpaRepository = termsJpaRepository;
	}

	@Override
	public List<Term> findActiveTerms(String termType) {
		List<TermsJpaEntity> entities = termType == null
				? termsJpaRepository.findByActiveTrueOrderByTermIdAsc()
				: termsJpaRepository.findByActiveTrueAndTermTypeIgnoreCaseOrderByTermIdAsc(termType);
		return entities.stream().map(this::toDomain).toList();
	}

	@Override
	public List<Term> findActiveRequiredTerms() {
		return termsJpaRepository.findByActiveTrueAndRequiredTrueOrderByTermIdAsc().stream()
				.map(this::toDomain)
				.toList();
	}

	@Override
	public Optional<Term> findByUuid(UUID termUuid) {
		return termsJpaRepository.findByTermUuid(termUuid.toString()).map(this::toDomain);
	}

	private Term toDomain(TermsJpaEntity entity) {
		return new Term(
				entity.getTermId(),
				UUID.fromString(entity.getTermUuid()),
				entity.getTitle(),
				entity.getTermType(),
				entity.getVersion(),
				entity.getContent(),
				entity.isRequired(),
				entity.isActive()
		);
	}
}
