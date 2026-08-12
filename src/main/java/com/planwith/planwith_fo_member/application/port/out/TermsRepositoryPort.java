package com.planwith.planwith_fo_member.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_member.domain.terms.Term;

public interface TermsRepositoryPort {

	List<Term> findActiveTerms(String termType);

	List<Term> findActiveRequiredTerms();

	Optional<Term> findByUuid(UUID termUuid);
}
