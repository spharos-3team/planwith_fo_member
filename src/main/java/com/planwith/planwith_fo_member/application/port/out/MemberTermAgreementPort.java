package com.planwith.planwith_fo_member.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberTermAgreementPort {

	void saveAgreements(UUID memberUuid, List<AgreementCommand> agreements);

	void upsertAgreements(UUID memberUuid, List<AgreementCommand> agreements);

	List<StoredAgreement> findByMemberUuid(UUID memberUuid);

	record AgreementCommand(Long termId, boolean agreed, Instant agreedAt) {
	}

	record StoredAgreement(Long termId, boolean agreed, Instant agreedAt) {
	}
}
