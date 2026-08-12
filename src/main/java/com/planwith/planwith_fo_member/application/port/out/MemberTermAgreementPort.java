package com.planwith.planwith_fo_member.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MemberTermAgreementPort {

	void saveAgreements(UUID memberUuid, List<AgreementCommand> agreements);

	record AgreementCommand(Long termId, boolean agreed, Instant agreedAt) {
	}
}
