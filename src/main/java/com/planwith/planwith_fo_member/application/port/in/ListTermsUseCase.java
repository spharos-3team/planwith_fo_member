package com.planwith.planwith_fo_member.application.port.in;

import java.util.List;

import com.planwith.planwith_fo_member.domain.terms.Term;

public interface ListTermsUseCase {

	List<Term> list(String termType);
}
