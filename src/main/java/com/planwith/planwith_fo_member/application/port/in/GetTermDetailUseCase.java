package com.planwith.planwith_fo_member.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_member.domain.terms.Term;

public interface GetTermDetailUseCase {

	Term get(UUID termUuid);
}
