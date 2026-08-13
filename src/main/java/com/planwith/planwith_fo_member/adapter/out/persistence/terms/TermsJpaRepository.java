package com.planwith.planwith_fo_member.adapter.out.persistence.terms;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsJpaRepository extends JpaRepository<TermsJpaEntity, Long> {

	List<TermsJpaEntity> findByActiveTrueOrderByTermIdAsc();

	List<TermsJpaEntity> findByActiveTrueAndTermTypeIgnoreCaseOrderByTermIdAsc(String termType);

	List<TermsJpaEntity> findByActiveTrueAndRequiredTrueOrderByTermIdAsc();

	Optional<TermsJpaEntity> findByTermUuid(String termUuid);

	List<TermsJpaEntity> findByTermIdIn(List<Long> termIds);
}
