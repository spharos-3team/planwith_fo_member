package com.planwith.planwith_fo_member.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MemberAgreementUseCase {

	List<AgreementView> list(UUID memberUuid);

	List<AgreementView> upsertOptional(UUID memberUuid, List<AgreementInput> agreements);

	record AgreementInput(UUID termUuid, boolean agreed) {
	}

	record AgreementView(
			UUID termUuid,
			String title,
			String termType,
			String version,
			boolean required,
			boolean agreed,
			Instant agreedAt
	) {
	}
}
